package com.tici.vpn.proxy.master.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.whenResumed
import com.common.baseui.BaseAppConfig
import com.tici.vpn.proxy.master.base.ProductActivity
import com.tici.vpn.proxy.master.databinding.ActivityMainBinding
import com.tici.vpn.proxy.master.home.HomeFragment
import com.tici.vpn.proxy.master.settings.SettingFragment
import com.tici.vpn.proxy.master.settings.appproxy.AppProxyFragment
import com.tici.vpn.proxy.master.settings.dns.DNSFragment
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.dialog.SettingSuccessDialog
import com.tici.vpn.proxy.master.remoteconfig.AdPlacementId
import com.tici.vpn.proxy.master.remoteconfig.FirebaseConfigManager
import com.simple.libads.AdNetworkType
import com.simple.libads.FrameAds
import com.simple.libads.base.bannerads.BannerLoader
import com.simple.libads.config.InterConfig
import com.simple.libads.manager.BannerManager
import com.simple.libads.setVisible
import com.tici.vpn.proxy.master.dialog.CloseAppDialog
import com.tici.vpn.proxy.master.home.BackActionBarFragment
import com.tici.vpn.proxy.master.home.ConnectedFragment
import com.tici.vpn.proxy.master.home.DisconnectedFragment
import com.tici.vpn.proxy.master.home.ProxyReport
import com.tici.vpn.proxy.master.network.ProxySpeedTest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ProductActivity<ActivityMainBinding>() {

    var isSettingFragment = false
    var isAppProxyFragment = false
    var isDnsFragment = false
    var isConnectedFragment = false
    var isDisconnectedFragment = false

    var isAutoConnect = false

    private var configAd: FirebaseConfigManager = FirebaseConfigManager.get()

    private val mainViewModel: MainViewModel by viewModels()

    private val mHomeFragment by lazy {
        HomeFragment.newInstance(
            showConnected = {
                proxy -> showConnected(proxy)
            },
            showDisconnected = {
                report -> showDisconnected(report)
            }
        )
    }

    private val mAppProxyFragment by lazy {
        AppProxyFragment(onSelect = {})
    }

    private val mDnsFragment by lazy {
        DNSFragment()
    }

    private val mSettingFragment by lazy {
        SettingFragment(showAppProxy = {
            showAppProxy()
        }, showDns = {
            showDns()
        })
    }

    private val mConnectedFragment by lazy {
        ConnectedFragment()
    }

    private val mDisonnectedFragment by lazy {
        DisconnectedFragment.newInstance(BaseAppConfig.proxyCountry)
    }

    companion object {
        private const val ADVERTISING_ID_PERMISSION = 1001
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    private var mBannerLoader: BannerLoader? = null

    override fun initView() {
        requestAdvertisingIdPermission()
        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        val reconnect = intent.getStringExtra("state")
        if (reconnect != null && reconnect == "POPUP") {
//            SettingSuccessDialog(
//                R.string.setting_choose_proxy, R.string.home_reconnect_to_take_effect
//            ).show(supportFragmentManager, "SettingSuccessDialog")
            isAutoConnect = true
        } else {
            isAutoConnect = false
        }
        intent.removeExtra("state")

        mainViewModel.checkActiveSubscriptions(applicationContext) {
            if (it) {
                binding.frameBanner.visibility = View.GONE
            }
        }

        mListFragment.add(mHomeFragment)
        mListFragment.add(mSettingFragment)
        mListFragment.add(mAppProxyFragment)
        mListFragment.add(mDnsFragment)
        mListFragment.add(mConnectedFragment)
        mListFragment.add(mDisonnectedFragment)

        showFragment(mHomeFragment)
//        binding.progress.setVisible(true)
        with(binding) {

            icBack.scaleX = if (isRTL) -1f else 1f
            ivBack.scaleX = if (isRTL) -1f else 1f
            ivBack2.scaleX = if (isRTL) -1f else 1f

            lnSetting.setOnClickListener {
                showFragment(mSettingFragment)
            }

            icBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            ivBack.setOnClickListener {
                showFragment(mSettingFragment)
            }

            ivBack2.setOnClickListener {
                showFragment(mHomeFragment)
            }

            selectAll.setOnClickListener {
                selectAll()
            }

            if (!BaseAppConfig.isSub) {
                initLoadAds(frameBanner)
            }
        }

        SharedData.isSub.observe(this) { it ->
//            Timber.d("Test_Subscribe isSub main_activity")
//            Log.i("SuperVpn", "TestRelease isSub main_activity $it")
            if (it == true) {
//                Timber.d("Test_Subscribe isSub main_activity true")
                binding.frameBanner.visibility = View.GONE
//                Toast.makeText(baseContext, com.tici.vpn.proxy.master.R.string.premium_purchase_success, Toast.LENGTH_SHORT).show()
            } else {
//                Timber.d("Test_Subscribe isSub main_activity false")
                binding.frameBanner.visibility = View.VISIBLE
                initLoadAds(binding.frameBanner)
            }
        }

        onBackPressedDispatcher.addCallback(
            this@MainActivity,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isSettingFragment) {
                        showFragment(mHomeFragment)
                    } else if (isAppProxyFragment) {
                        mAppProxyFragment.hideKeyboard()
                        showFragment(mSettingFragment)
                    } else if (isDnsFragment) {
                        showFragment(mSettingFragment)
                    } else if (isConnectedFragment) {
                        showFragment(mHomeFragment)
                    } else if (isDisconnectedFragment) {
                        showFragment(mHomeFragment)
                    } else {
                        CloseAppDialog (
                            onClose = {
                                Timber.d("exit finishAffinity")
                                finishAffinity()
                            }
                        ).show(supportFragmentManager, "CloseApp")
                    }
                }
            })
    }

    private fun initLoadAds(frameBanner: FrameAds) {
        configAd.adBanner.find { it.placementId == AdPlacementId.BANNER_HOME }?.let {
            mBannerLoader = BannerManager.createLoader(adNetwork = it.adNetwork, bannerId = it.adId)
            mBannerLoader?.canRequest = true
            try {
                mBannerLoader?.loadAds(
                    context = this@MainActivity, bannerType = it.adType, parent = frameBanner
                )
            } catch (_: Exception) {
            }
        }
    }

    private var mListFragment = arrayListOf<Fragment>()

    @SuppressLint("CommitTransaction")
    fun showFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val otherFragment = mListFragment.filter { it != fragment }

        isAppProxyFragment = fragment is AppProxyFragment

        if (fragment.isAdded) {
            if (isAppProxyFragment) {
                mAppProxyFragment.loadData()
            }
            fragmentTransaction.show(fragment)
        } else {
            fragmentTransaction.add(binding.frameContainer.id, fragment)
        }

        otherFragment.forEach {
            if (it.isAdded) {
                fragmentTransaction.hide(it)
            }
        }

        lifecycleScope.launch {
            lifecycle.whenResumed {
                fragmentTransaction.commitNow()
            }
        }
//        fragmentTransaction.commitAllowingStateLoss()

            isAppProxyFragment = false
            isSettingFragment = false

            when (fragment) {
                is AppProxyFragment -> {
                    isAppProxyFragment = true
                    isSettingFragment = false
                    isDnsFragment = false
                    isConnectedFragment = false
                    isDisconnectedFragment = false
                    with(binding) {
                        lbSetting.text = getString(R.string.setting_app_proxy)
                        homeActionBar.setVisible(false)
                        subActionbar.setVisible(true)
                        dnsActionBar.setVisible(false)
                        selectAll.setVisible(true)
                        backActionBar.setVisible(false)
                        actionBar.setBackgroundColor(resources.getColor(R.color.gray_3))
                    }
                }

                is SettingFragment -> {
                    isSettingFragment = true
                    isAppProxyFragment = false
                    isDnsFragment = false
                    isConnectedFragment = false
                    isDisconnectedFragment = false
                    with(binding) {
                        lbSetting.text = getString(R.string.setting)
                        homeActionBar.setVisible(false)
                        subActionbar.setVisible(true)
                        dnsActionBar.setVisible(false)
                        selectAll.setVisible(false)
                        backActionBar.setVisible(false)
                        actionBar.setBackgroundColor(resources.getColor(R.color.gray_3))
                    }
                }

                is DNSFragment -> {
                    isAppProxyFragment = false
                    isDnsFragment = true
                    isSettingFragment = false
                    isConnectedFragment = false
                    isDisconnectedFragment = false
                    with(binding) {
                        homeActionBar.setVisible(false)
                        subActionbar.setVisible(false)
                        dnsActionBar.setVisible(true)
                        //ivSwitch.isSelected = BaseAppConfig.dnsServer.isNotEmpty()
                        ivSwitch.setVisible(false)
                        selectAll.setVisible(false)
                        backActionBar.setVisible(false)
                        actionBar.setBackgroundColor(resources.getColor(R.color.gray_3))
                    }
                }

                is HomeFragment -> {
                    isConnectedFragment = false
                    isDisconnectedFragment = false
                    with(binding) {
                        homeActionBar.setVisible(true)
                        subActionbar.setVisible(false)
                        dnsActionBar.setVisible(false)
                        selectAll.setVisible(false)
                        backActionBar.setVisible(false)
                        actionBar.setBackgroundColor(resources.getColor(R.color.backgroundColor))
                    }
                }

                else -> {
                    with(binding) {
                        homeActionBar.setVisible(false)
                        subActionbar.setVisible(false)
                        dnsActionBar.setVisible(false)
                        selectAll.setVisible(false)
                        backActionBar.setVisible(true)
                        tvTitle2.text = (fragment as? BackActionBarFragment)?.getTitle()
                        actionBar.setBackgroundColor(resources.getColor(R.color.backgroundColor))
                    }
                }
            }
        }

        private fun showAppProxy() {
//        mainViewModel.getInstalledAppsWithInternetPermission(baseContext)
            showFragment(mAppProxyFragment)
            mAppProxyFragment.loadData()
            mAppProxyFragment.clearUI()
        }

        private fun showDns() {
            if (mDnsFragment.isNeedReload) {
                mDnsFragment.loadData()
            }
            showFragment(mDnsFragment)
        }

        private fun showConnected(proxy: ProxySpeedTest.ProxyConfig?) {
            mConnectedFragment.setCurrentProxy(proxy)
            isConnectedFragment = true
            showFragment(mConnectedFragment)
        }

        private fun showDisconnected(report: ProxyReport) {
            mDisonnectedFragment.loadData(report)
            isDisconnectedFragment = true
            showFragment(mDisonnectedFragment)
        }

        private fun selectAll() {
            mAppProxyFragment.checkUncheckAll()
        }

        private fun checkAdvertisingIdPermission(): Boolean {
            return checkSelfPermission(android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
        }

        private fun requestAdvertisingIdPermission() {
            if (!checkAdvertisingIdPermission()) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.INTERNET), ADVERTISING_ID_PERMISSION
                )
            }
        }

        override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<out String>, grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            when (requestCode) {
                ADVERTISING_ID_PERMISSION -> {
                    if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(
                            this,
                            "Permission denied. Some features may not work properly",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        override fun interConfigs(): List<InterConfig>? {
            return configAd.adWithoutVideoPlacement.placementIds.map {
                InterConfig(
                    adid = configAd.adWithVideo.adId,
                    adnetwork = AdNetworkType.ADMOB,
                    placement_id = it
                )
            }
        }
    }
package com.tici.vpn.proxy.master.main

import android.annotation.SuppressLint
import android.app.ComponentCaller
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import com.common.baseui.BaseAppConfig
import com.core.baseui.BaseActivity
import com.core.baseui.BillingViewModel
import com.core.config.domain.data.IAdPlaceName
import com.simple.libads.setVisible
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.ActivityMainBinding
import com.tici.vpn.proxy.master.feature.feature_exit.DialogFragmentExitApp
import com.tici.vpn.proxy.master.home.BackActionBarFragment
import com.tici.vpn.proxy.master.home.ConnectedFragment
import com.tici.vpn.proxy.master.home.DisconnectedFragment
import com.tici.vpn.proxy.master.home.HomeFragment
import com.tici.vpn.proxy.master.home.ProxyReport
import com.tici.vpn.proxy.master.network.ProxySpeedTest
import com.tici.vpn.proxy.master.remoteconfig.FirebaseConfigManager
import com.tici.vpn.proxy.master.required.ads.AppAdPlaceName
import com.tici.vpn.proxy.master.required.inapp.InAppBillingViewModel
import com.tici.vpn.proxy.master.settings.SettingFragment
import com.tici.vpn.proxy.master.settings.appproxy.AppProxyFragment
import com.tici.vpn.proxy.master.settings.dns.DNSFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    var isSettingFragment = false
    var isAppProxyFragment = false
    var isDnsFragment = false
    var isConnectedFragment = false
    var isDisconnectedFragment = false

    private val inAppBillingViewModel: BillingViewModel by viewModels<InAppBillingViewModel>()

//    var isAutoConnect = false

    private var configAd: FirebaseConfigManager = FirebaseConfigManager.get()

    private val mainViewModel: MainViewModel by viewModels()

    private val mHomeFragment by lazy {
        /*HomeFragment.newInstance(
            showConnected = { proxy ->
                showConnected(proxy)
            },
            showDisconnected = { report ->
                showDisconnected(report)
            }
        )*/
        HomeFragment()
    }

    private val mAppProxyFragment by lazy {
        AppProxyFragment()
    }

    private val mDnsFragment by lazy {
        DNSFragment()
    }

    private val mSettingFragment by lazy {
        SettingFragment()
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

    override fun providerInterAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            AppAdPlaceName.FULLSCREEN_BACK_MAIN
        )
    }

    override fun onBackPressed() {
        val fragmentCurrent = supportFragmentManager.findFragmentById(binding.frameContainer.id)
        // Kiểm tra nếu activity đang ở top của stack
        if (fragmentCurrent == null || fragmentCurrent is HomeFragment) {
            val dialogFragmentExitApp = DialogFragmentExitApp()
            dialogFragmentExitApp.show(
                supportFragmentManager,
                DialogFragmentExitApp::class.java.simpleName
            )
        } else {
            super.onBackPressed() // Thực hiện hành động back bình thường
        }
    }

    override fun initViews(savedInstanceState: Bundle?) {
        requestAdvertisingIdPermission()
        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        val reconnect = intent.getStringExtra("state")

        mListFragment.add(mHomeFragment)
        mListFragment.add(mSettingFragment)
        mListFragment.add(mAppProxyFragment)
        mListFragment.add(mDnsFragment)
        mListFragment.add(mConnectedFragment)
        mListFragment.add(mDisonnectedFragment)

        showFragment(mHomeFragment)
        with(binding) {

            icBack.scaleX = if (isRTL) -1f else 1f
            ivBack.scaleX = if (isRTL) -1f else 1f
            ivBack2.scaleX = if (isRTL) -1f else 1f

            lnSetting.setOnClickListener {
                showFragment(mSettingFragment)
            }

            icBack.setOnClickListener {
                showInterAd(AppAdPlaceName.FULLSCREEN_BACK_MAIN) {}
                onBackPressedDispatcher.onBackPressed()
            }

            ivBack.setOnClickListener {
                showInterAd(AppAdPlaceName.FULLSCREEN_BACK_MAIN) {}
                showFragment(mSettingFragment)
            }

            ivBack2.setOnClickListener {
                showInterAd(AppAdPlaceName.FULLSCREEN_BACK_MAIN) {}
                showFragment(mHomeFragment)
            }

            selectAll.setOnClickListener {
                selectAll()
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
//<<<<<<< HEAD
//                        CloseAppDialog(
//                            onClose = {
//                                Timber.d("exit finishAffinity")
//                                finishAffinity()
//                            }
//                        ).show(supportFragmentManager, "CloseApp")
//=======
                        val dialogFragmentExitApp = DialogFragmentExitApp()
                        dialogFragmentExitApp.show(
                            supportFragmentManager,
                            DialogFragmentExitApp::class.java.simpleName
                        )
//>>>>>>> feature/08092025_update_core_base
                    }
                }
            })
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
//<<<<<<< HEAD
                    subActionbar.setBackgroundColor(resources.getColor(R.color.backgroundColor))
//=======
//>>>>>>> feature/08092025_update_core_base
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
//<<<<<<< HEAD
                    dnsActionBar.setBackgroundColor(resources.getColor(R.color.backgroundColor))
//=======
//>>>>>>> feature/08092025_update_core_base
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

    //<<<<<<< HEAD
//    private fun showAppProxy() {
//=======
    fun showAppProxy() {
//>>>>>>> feature/08092025_update_core_base
//        mainViewModel.getInstalledAppsWithInternetPermission(baseContext)
        showFragment(mAppProxyFragment)
        mAppProxyFragment.loadData()
        mAppProxyFragment.clearUI()
    }

    //<<<<<<< HEAD
//    private fun showDns() {
//=======
    fun showDns() {
//>>>>>>> feature/08092025_update_core_base
        if (mDnsFragment.isNeedReload) {
            mDnsFragment.loadData()
        }
        showFragment(mDnsFragment)
    }

    fun showConnected(proxy: ProxySpeedTest.ProxyConfig?) {
        mConnectedFragment.setCurrentProxy(proxy)
        isConnectedFragment = true
        showFragment(mConnectedFragment)
    }

    fun showDisconnected(report: ProxyReport) {
        mDisonnectedFragment.loadData(report)
        isDisconnectedFragment = true
        showFragment(mDisonnectedFragment)
    }

    fun navigateHome() {
        showFragment(mHomeFragment)
    }

    override fun onResume() {
        super.onResume()
        inAppBillingViewModel.restorePurchasedIfVipReady(false, false)
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
//<<<<<<< HEAD

//    override fun interConfigs(): List<InterConfig>? {
//        return configAd.adWithoutVideoPlacement.placementIds.map {
//            InterConfig(
//                adid = configAd.adWithVideo.adId,
//                adnetwork = AdNetworkType.ADMOB,
//                placement_id = it
//            )
//        }
//    }
//=======
//>>>>>>> feature/08092025_update_core_base
}
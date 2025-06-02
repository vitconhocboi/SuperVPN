package com.highsecure.vpn.proxy.master.main

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.highsecure.vpn.proxy.master.base.ProductActivity
import com.highsecure.vpn.proxy.master.databinding.ActivityMainBinding
import com.highsecure.vpn.proxy.master.home.HomeFragment
import com.highsecure.vpn.proxy.master.settings.SettingFragment
import com.highsecure.vpn.proxy.master.settings.appproxy.AppProxyFragment
import com.highsecure.vpn.proxy.master.settings.dns.DNSFragment
import com.highsecure.vpn.proxy.master.R
import com.highsecure.vpn.proxy.master.remoteconfig.AdPlacementId
import com.highsecure.vpn.proxy.master.remoteconfig.FirebaseConfigManager
import com.simple.libads.AdNetworkType
import com.simple.libads.AdsActivity
import com.simple.libads.base.bannerads.BannerLoader
import com.simple.libads.config.InterConfig
import com.simple.libads.manager.BannerManager
import com.simple.libads.setVisible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ProductActivity<ActivityMainBinding>() {

    var isSettingFragment = false
    var isAppProxyFragment = false

    var configAd: FirebaseConfigManager = FirebaseConfigManager.get()

    private val mainViewModel: MainViewModel by viewModels()

    private val mHomeFragment by lazy {
        HomeFragment()
    }

    private val mAppProxyFragment by lazy {
        AppProxyFragment(
            onSelect = {}
        )
    }

    private val mDnsFragment by lazy {
        DNSFragment()
    }

    private val mSettingFragment by lazy {
        SettingFragment(
            showAppProxy = {
                showAppProxy()
            },
            showDns = {
                showDns()
            }
        )
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

        mListFragment.add(mHomeFragment)
        mListFragment.add(mSettingFragment)
        mListFragment.add(mAppProxyFragment)
        mListFragment.add(mDnsFragment)

        showFragment(mHomeFragment)
//        binding.progress.setVisible(true)
        with(binding) {
            lnSetting.setOnClickListener {
                showFragment(mSettingFragment)
            }

            icBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            ivBack.setOnClickListener {
                showFragment(mSettingFragment)
            }

            selectAll.setOnClickListener {
                selectAll()
            }

            configAd.adBanner.find { it.placementId == AdPlacementId.BANNER_HOME }?.let {
                mBannerLoader = BannerManager.createLoader(adNetwork = it.adNetwork, bannerId = it.adId)
                mBannerLoader?.canRequest = true
                mBannerLoader?.loadAds(
                    context = this@MainActivity, bannerType = it.adType, parent = frameBanner
                )
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
                    } else {
                        finishAffinity()
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

        fragmentTransaction.commitNow()

        isAppProxyFragment = false
        isSettingFragment = false

        when (fragment) {
            is AppProxyFragment -> {
                isAppProxyFragment = true
                with(binding) {
                    lbSetting.text = getString(R.string.setting_app_proxy)
                    homeActionBar.setVisible(false)
                    subActionbar.setVisible(true)
                    dnsActionBar.setVisible(false)
                    selectAll.setVisible(true)
                    actionBar.setBackgroundColor(resources.getColor(R.color.gray_3))
                }
            }

            is SettingFragment -> {
                isSettingFragment = true
                with(binding) {
                    lbSetting.text = getString(R.string.setting)
                    homeActionBar.setVisible(false)
                    subActionbar.setVisible(true)
                    dnsActionBar.setVisible(false)
                    selectAll.setVisible(false)
                    actionBar.setBackgroundColor(resources.getColor(R.color.gray_3))
                }
            }

            is DNSFragment -> {
                isAppProxyFragment = true
                with(binding) {
                    homeActionBar.setVisible(false)
                    subActionbar.setVisible(false)
                    dnsActionBar.setVisible(true)
                    //ivSwitch.isSelected = BaseAppConfig.dnsServer.isNotEmpty()
                    ivSwitch.setVisible(false)
                    selectAll.setVisible(false)
                    actionBar.setBackgroundColor(resources.getColor(R.color.gray_3))
                }
            }

            else -> {
                with(binding) {
                    homeActionBar.setVisible(true)
                    subActionbar.setVisible(false)
                    dnsActionBar.setVisible(false)
                    selectAll.setVisible(false)
                    actionBar.setBackgroundColor(resources.getColor(R.color.backgroundColor))
                }
            }
        }
    }

    fun showAppProxy() {
//        mainViewModel.getInstalledAppsWithInternetPermission(baseContext)
        showFragment(mAppProxyFragment)
        mAppProxyFragment.loadData()
        mAppProxyFragment.clearUI()
    }

    fun showDns() {
        showFragment(mDnsFragment)
    }

    fun selectAll() {
        mAppProxyFragment.checkUncheckAll()
    }

    private fun checkAdvertisingIdPermission(): Boolean {
        return checkSelfPermission(android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAdvertisingIdPermission() {
        if (!checkAdvertisingIdPermission()) {
            requestPermissions(
                arrayOf(android.Manifest.permission.INTERNET),
                ADVERTISING_ID_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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
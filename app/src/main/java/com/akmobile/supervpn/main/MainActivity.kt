package com.akmobile.supervpn.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.akmobile.supervpn.base.ProductActivity
import com.akmobile.supervpn.databinding.ActivityMainBinding
import com.akmobile.supervpn.home.HomeFragment
import com.akmobile.supervpn.settings.SettingFragment
import com.akmobile.supervpn.settings.appproxy.AppProxyFragment
import com.akmobile.supervpn.settings.dns.DNSFragment
import com.common.baseui.BaseAppConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.simple.libads.setVisible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ProductActivity<ActivityMainBinding>() {

    var isSettingFragment = false
    var isAppProxyFragment = false

    private val mainViewModel: MainViewModel by viewModels()

    private val mHomeFragment by lazy {
        HomeFragment()
    }

    private val mAppProxyFragment by lazy {
        AppProxyFragment()
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

//            cbSelectAll.setOnClickListener {
//
//            }
        }

        onBackPressedDispatcher.addCallback(
            this@MainActivity,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isSettingFragment) {
                        showFragment(mHomeFragment)
                    } else if (isAppProxyFragment) {
                        showFragment(mSettingFragment)
                    } else {
                        this@MainActivity.finish()
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

        when (fragment) {
            is AppProxyFragment -> {
                binding.homeActionBar.setVisible(false)
                binding.subActionbar.setVisible(true)
                binding.dnsActionBar.setVisible(false)
            }

            is SettingFragment -> {
                binding.homeActionBar.setVisible(false)
                binding.subActionbar.setVisible(true)
                binding.dnsActionBar.setVisible(false)
            }

            is DNSFragment -> {
                binding.homeActionBar.setVisible(false)
                binding.subActionbar.setVisible(false)
                binding.dnsActionBar.setVisible(true)
                binding.ivSwitch.isSelected = BaseAppConfig.dnsServer.isNotEmpty()
            }

            else -> {
                binding.homeActionBar.setVisible(true)
                binding.subActionbar.setVisible(false)
                binding.dnsActionBar.setVisible(false)
            }
        }
        isSettingFragment = fragment is SettingFragment
//
//        binding.homeActionBar.setVisible(!isSettingFragment && !isAppProxyFragment)
//        binding.cbSelectAll.setVisible(false)
//        binding.subActionbar.setVisible(isSettingFragment || isAppProxyFragment)
//        binding.dnsActionBar.setVisible(fragment is DNSFragment)
    }

    fun showAppProxy() {
//        mainViewModel.getInstalledAppsWithInternetPermission(baseContext)
        showFragment(mAppProxyFragment)
        mAppProxyFragment.loadData()
    }

    fun showDns() {
        showFragment(mDnsFragment)
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
                    Toast.makeText(this, "Permission denied. Some features may not work properly", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
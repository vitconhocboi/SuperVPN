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
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.simple.libads.setVisible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ProductActivity<ActivityMainBinding>() {

    var isSettingFragment = false
    var isAppProxyFragment = false

    private val mainViewModel : MainViewModel by viewModels()

    private val mHomeFragment by lazy {
        HomeFragment()
    }

    private val mAppProxyFragment by lazy {
        AppProxyFragment()
    }

    private val mSettingFragment by lazy {
        SettingFragment {
            showAppProxy()
        }
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initView() {
        mListFragment.add(mHomeFragment)
        mListFragment.add(mSettingFragment)
        mListFragment.add(mAppProxyFragment)

        showFragment(mHomeFragment)
//        binding.progress.setVisible(true)
        with(binding) {
            lnSetting.setOnClickListener {
                showFragment(mSettingFragment)
            }

            icBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            cbSelectAll.setOnClickListener {

            }
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

        isSettingFragment = fragment is SettingFragment

        binding.homeActionBar.setVisible(!isSettingFragment && !isAppProxyFragment)
        binding.cbSelectAll.setVisible(false)
        binding.subActionbar.setVisible(isSettingFragment || isAppProxyFragment)
    }

    fun showAppProxy() {
//        mainViewModel.getInstalledAppsWithInternetPermission(baseContext)
        showFragment(mAppProxyFragment)
        mAppProxyFragment.loadData()
    }
}
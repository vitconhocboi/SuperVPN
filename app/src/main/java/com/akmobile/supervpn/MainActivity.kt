package com.akmobile.supervpn

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.akmobile.supervpn.databinding.ActivityMainBinding
import com.akmobile.supervpn.home.HomeFragment
import com.akmobile.supervpn.proxy.ProxyFragment
import com.akmobile.supervpn.settings.SettingFragment
import com.common.baseui.extension.context
import com.simple.libads.setVisible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ProductActivity<ActivityMainBinding>() {

    var isSettingFragment = false

    private val mHomeFragment by lazy {
        HomeFragment()
    }

    private val mSettingFragment by lazy {
        SettingFragment()
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initView() {
        mListFragment.add(mHomeFragment)
        mListFragment.add(mSettingFragment)
        showFragment(mHomeFragment)
        with(binding) {
            lnSetting.setOnClickListener {
                showFragment(mSettingFragment)
            }

            icBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }

        onBackPressedDispatcher.addCallback(
            this@MainActivity,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                        if (isSettingFragment) {
                            showFragment(mHomeFragment)
                        } else {
                            this@MainActivity.finish()
                        }
                    }

            })
    }

    private var mListFragment = arrayListOf<androidx.fragment.app.Fragment>()

    @SuppressLint("CommitTransaction")
    fun showFragment(fragment: androidx.fragment.app.Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val otherFragment = mListFragment.filter { it != fragment }

        if (fragment.isAdded) {
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

        binding.homeActionBar.setVisible(!isSettingFragment)
        binding.subActionbar.setVisible(isSettingFragment)
    }
}
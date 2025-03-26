package com.akmobile.supervpn

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import com.akmobile.supervpn.databinding.ActivityMainBinding
import com.akmobile.supervpn.home.HomeFragment
import com.akmobile.supervpn.proxy.ProxyFragment
import com.akmobile.supervpn.settings.SettingFragment

class MainActivity : ProductActivity<ActivityMainBinding>() {
    private val mHomeFragment by lazy {
        HomeFragment()
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initView() {
        showFragment(mHomeFragment)
        with(binding) {
            lnSetting.setOnClickListener {
                showFragment(SettingFragment())
            }
        }
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
    }

    private fun getInstalledAppsWithInternetPermission(): List<ApplicationInfo> {
        val packageManager = packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        return installedApps.filter { appInfo ->
            packageManager.checkPermission(
                android.Manifest.permission.INTERNET,
                appInfo.packageName
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
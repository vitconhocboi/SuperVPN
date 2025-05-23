package com.akmobile.supervpn.settings.appproxy

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.akmobile.supervpn.base.ProductFragment
import com.akmobile.supervpn.databinding.FragmentAppProxyBinding
import com.akmobile.supervpn.proxy.ProxyUI
import com.common.baseui.extension.bindFlowCreate
import com.common.baseui.extension.setVisible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppProxyFragment(
    val onSelect : () -> Unit
) :
    ProductFragment<FragmentAppProxyBinding>() {

    @Inject
    lateinit var appProxyAdapter: AppProxyAdapter

    private val allowAppViewModel: AppProxyViewModel by viewModels()

    private val selectedList = ArrayList<AppProxyUI>()

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentAppProxyBinding {
        return FragmentAppProxyBinding.inflate(inflater, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initView() {
        super.initView()
        appProxyAdapter = AppProxyAdapter()
//        bindFlowCreate(allowAppViewModel.allowApp) { allowApp ->
//            if (allowApp.data?.isEmpty() == true) {
//                val listMergeAllowApp = appProxyAdapter.datas.map { app ->
//                    app?.allowed = true
//                    app
//                }
//                appProxyAdapter.updateData(listMergeAllowApp)
//            } else {
//                val listMergeAllowApp = appProxyAdapter.datas.map { app ->
//                    app?.allowed =
//                        (allowApp.data?.find { allowed -> app?.packageName == allowed.packageName } != null)
//                    app
//                }
//                appProxyAdapter.updateData(listMergeAllowApp)
//            }
//        }
        appProxyAdapter.onItemClick = { _, item ->
//            allowAppViewModel.setAllowApp(item)
            selectedList.add(item)
        }
        binding.apply {
            recyclerViewApps.adapter = appProxyAdapter

            btnOk.setOnClickListener {
                val listAllows = ArrayList<AppProxyUI>()
                appProxyAdapter.datas.map { it ->
                    if (it!!.allowed == true)
                        listAllows.add(it)
                }
                allowAppViewModel.setAllowApps(listAllows)
                Toast.makeText(requireContext(), "Save successfully", Toast.LENGTH_SHORT).show()
                onBackPress()
            }

            btnCancel.setOnClickListener {
                loadData()
            }
        }

        allowAppViewModel.isLoading.observe(this) { it ->
            if (it == true) {
                binding.progress.setVisible(true)
            } else {
                binding.progress.setVisible(false)
            }
        }

        allowAppViewModel.loadAppsDone.observe(this) { it ->
            if (it == true) {
                if (allowAppViewModel.allowApp.value.data?.isEmpty() == true) {
                    val listMergeAllowApp = appProxyAdapter.datas.map { app ->
                        app?.allowed = true
                        if (!selectedList.contains(app!!)) {
                            selectedList.add(app)
                        }
                        app
                    }
                    appProxyAdapter.updateData(listMergeAllowApp)
                } else {
                    val listMergeAllowApp = appProxyAdapter.datas.map { app ->
                        app?.allowed =
                            (allowAppViewModel.allowApp.value.data?.find { allowed -> app?.packageName == allowed.packageName } != null)
                        if (app?.allowed == true && !selectedList.contains(app)) {
                            selectedList.add(app)
                        }
                        app
                    }
                    appProxyAdapter.updateData(listMergeAllowApp)
                }
            }
        }

        allowAppViewModel.listApps.observe(this) { it ->
//            if (it != null && it.isNotEmpty()) {
            appProxyAdapter.updateData(it)
            allowAppViewModel.getAllowApp()
//            }
        }

//        loadData()
    }

    override fun onResume() {
        super.onResume()
    }

    fun loadData() {
        selectedList.clear()
        allowAppViewModel.getInstalledAppsWithInternetPermission(requireContext())
    }

    @SuppressLint("NotifyDataSetChanged")
    fun checkUncheckAll() {
        if (selectedList.isNotEmpty() && selectedList.size != appProxyAdapter.datas.size) {
            appProxyAdapter.datas.map {
                if (!selectedList.contains(it)) selectedList.add(it!!)
                it!!.allowed = true
            }
        } else if (selectedList.isNotEmpty()){
            selectedList.clear()
            appProxyAdapter.datas.map { it!!.allowed = false }
        } else {
            appProxyAdapter.datas.map {
                selectedList.add(it!!)
                it.allowed = true
            }
        }
        appProxyAdapter.notifyDataSetChanged()
    }
}
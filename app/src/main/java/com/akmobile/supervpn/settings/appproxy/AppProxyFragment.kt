package com.akmobile.supervpn.settings.appproxy

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.akmobile.supervpn.base.ProductFragment
import com.akmobile.supervpn.databinding.FragmentAppProxyBinding
import com.akmobile.supervpn.proxy.ProxyUI
import com.akmobile.supervpn.utils.hideKeyboard
import com.akmobile.supervpn.utils.textChanges
import com.akmobile.supervpn.utils.Navigator
import com.common.baseui.extension.bindFlowCreate
import com.common.baseui.extension.hideKeyBoard
import com.common.baseui.extension.setVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("NotifyDataSetChanged")
    override fun initView() {
        super.initView()

        binding.edtSearch.clearFocus()
        binding.edtSearch.hideKeyboard()

        appProxyAdapter = AppProxyAdapter()
        appProxyAdapter.onItemClick = { _, item ->
            if (item.allowed) {
                if (!selectedList.contains(item)) {
                    selectedList.add(item)
                }
            } else {
                if (selectedList.contains(item)) {
                    selectedList.remove(item)
                }
            }
        }
        binding.apply {
            recyclerViewApps.adapter = appProxyAdapter

            btnOk.setOnClickListener {
                edtSearch.hideKeyboard()
                val listDisallows = ArrayList<AppProxyUI>()
                val listFromBaseList = appProxyAdapter.listRecord.filter { !selectedList.contains(it)}
                listDisallows.addAll(listFromBaseList as Collection<out AppProxyUI>)
                allowAppViewModel.setDisallowApps(listDisallows)
                Toast.makeText(requireContext(), "Save successfully", Toast.LENGTH_SHORT).show()
//                requireActivity().finish()
//                Navigator.startMainActivity(requireContext(), "RECONNECT")
            }

            btnCancel.setOnClickListener {
                edtSearch.hideKeyboard()
                loadData()
                activity?.onBackPressedDispatcher?.onBackPressed()
            }

            edtSearch.textChanges (lifecycleScope) {
                if (appProxyAdapter.listRecord.isNotEmpty()) {
                    appProxyAdapter.filter(it)
                }
            }
        }

        allowAppViewModel.isLoading.observe(this) { it ->
            if (it == true) {
                binding.edtSearch.visibility = View.GONE
                binding.progress.setVisible(true)
            } else {
                binding.edtSearch.visibility = View.VISIBLE
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
                    updateAdapter(listMergeAllowApp)
//                    appProxyAdapter.setData(listMergeAllowApp)
//                    appProxyAdapter.updateData(listMergeAllowApp)
                } else {
                    val listMergeAllowApp = appProxyAdapter.datas.map { app ->
                        app?.allowed =
                            allowAppViewModel.allowApp.value.data?.find { allowed -> app?.packageName == allowed.packageName } == null
                        if (app?.allowed == true && !selectedList.contains(app)) {
                            selectedList.add(app)
                        }
                        app
                    }
                    updateAdapter(listMergeAllowApp)
//                    appProxyAdapter.setData(listMergeAllowApp)
//                    appProxyAdapter.updateData(listMergeAllowApp)
                }
            }
        }

        allowAppViewModel.listApps.observe(this) { it ->
//            appProxyAdapter.setData(it)
//            appProxyAdapter.updateData(it)
            updateAdapter(it)
            allowAppViewModel.getAllowApp()
        }
    }

    fun updateAdapter(list: List<AppProxyUI?>) {
        appProxyAdapter.setData(list)
        appProxyAdapter.updateData(list)
    }

    override fun onResume() {
        super.onResume()
        binding.edtSearch.setText("")
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
        appProxyAdapter.setData(appProxyAdapter.datas)
        appProxyAdapter.notifyDataSetChanged()
    }

    fun hideKeyboard() {
        binding.edtSearch.hideKeyboard()
    }

    fun clearUI() {
        binding.edtSearch.setText("")
    }
}
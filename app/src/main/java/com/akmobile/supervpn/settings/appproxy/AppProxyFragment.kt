package com.akmobile.supervpn.settings.appproxy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.akmobile.supervpn.ProductFragment
import com.akmobile.supervpn.databinding.FragmentAppProxyBinding
import com.common.baseui.extension.bindFlowCreate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppProxyFragment(val list: ArrayList<AppProxyUI>) :
    ProductFragment<FragmentAppProxyBinding>() {

    @Inject
    lateinit var appProxyAdapter: AppProxyAdapter

    private val allowAppViewModel: AppProxyViewModel by viewModels()

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentAppProxyBinding {
        return FragmentAppProxyBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        appProxyAdapter = AppProxyAdapter()
        allowAppViewModel.getAllowApp()
        bindFlowCreate(allowAppViewModel.allowApp) { allowApp ->
            val listMergeAllowApp = list.map { app ->
                app.allowed =
                    (allowApp.data?.find { allowed -> app.packageName == allowed.packageName } != null)
                app
            }
            appProxyAdapter.updateData(listMergeAllowApp)
        }
        appProxyAdapter.onItemClick = { _, item ->
            allowAppViewModel.setAllowApp(item)
        }
        binding.apply {
            recyclerViewApps.adapter = appProxyAdapter

            btnOk.setOnClickListener {

            }

            btnCancel.setOnClickListener {

            }
        }
    }
}
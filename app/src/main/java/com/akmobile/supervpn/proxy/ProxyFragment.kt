package com.akmobile.supervpn.proxy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.akmobile.supervpn.base.ProductFragment
import com.akmobile.supervpn.databinding.FragmentProxyBinding
import com.akmobile.supervpn.db.VpnAppItemDB
import com.akmobile.supervpn.settings.appproxy.AppProxyViewModel
import com.akmobile.supervpn.utils.Navigator
import com.common.baseui.extension.bindFlowCreate
import com.common.baseui.extension.processResultData
import com.common.baseui.extension.setVisible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProxyFragment : ProductFragment<FragmentProxyBinding>() {

    @Inject
    lateinit var mPagerAdapter: ProxyGroupAdapter

    private val mProxyViewModel: ProxyViewModel by viewModels()


    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentProxyBinding {
        return FragmentProxyBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        with(binding) {
            ivBack.setOnClickListener {
                Navigator.startMainActivity(requireActivity(), null)
            }
            ivReload.setOnClickListener {

            }
            rcvGroupProxy.adapter = mPagerAdapter
            mProxyViewModel.getAllProxy(requireContext())
//            bindFlowCreate(mProxyViewModel.allProxy) { result ->
//                processResultData(result, onSuccess = {
//                    mPagerAdapter.updateData(it)
//                })
//            }

            mProxyViewModel.allProxy.observe(viewLifecycleOwner) { it ->
                if (it.isNotEmpty()) {
                    mPagerAdapter.updateData(it)
                }
                progress.setVisible(false)
            }

            mPagerAdapter.onItemClick = { position, item ->
                if (item.active) {
                    mProxyViewModel.setActiveProxy(item)
                } else {
                    mProxyViewModel.setActiveProxy(null)
                }
                mPagerAdapter.notifyDataSetChanged()
                Navigator.startMainActivity(requireContext(), item.id)
            }
        }
    }
}
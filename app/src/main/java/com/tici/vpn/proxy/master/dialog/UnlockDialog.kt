package com.tici.vpn.proxy.master.dialog

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.common.baseui.extension.context
import com.tici.vpn.proxy.master.databinding.DialogRateCompleteBinding
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.DialogUnlockBinding
import com.tici.vpn.proxy.master.proxy.ProxyGroupUI
import com.tici.vpn.proxy.master.utils.Navigator
import com.tici.vpn.proxy.master.utils.Utils

class UnlockDialog(
    val data: ProxyGroupUI
) : BottomSheetDialogFragment() {
    private var _binding: DialogUnlockBinding? = null
    protected val binding: DialogUnlockBinding
        get() = _binding
            ?: throw RuntimeException("Should only use binding after onCreateView and before onDestroyView")

    fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): DialogRateCompleteBinding {
        return DialogRateCompleteBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogUnlockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.unlock_bg)
        val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        roundedDrawable.cornerRadius = 20f

        binding.apply {
//            tvSubmit.setOnClickNoDoubleClick { dismiss() }
//            tvSubmit.isSelected = true
            ivFlag.setImageResource(Utils.getFlag(data.country))
            countryTitle.text = getStringSafely(context = context, name = data.country) ?: "undefined"
            lnGetPremium.background = roundedDrawable

            lnGetPremium.setOnClickListener {
                Navigator.startPremiumActivity(requireContext())
                dismiss()
            }

            btnClose.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun getStringSafely(context: Context, name: String): String? {
        val resId = context.resources.getIdentifier(name, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
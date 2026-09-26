package com.tici.vpn.proxy.master.settings.adsblock

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.setVisible
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.google.android.material.snackbar.Snackbar
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.FragmentAdsBlockBinding
import com.tici.vpn.proxy.master.network.MitmCaStore
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import com.tici.vpn.proxy.master.utils.hideKeyboard
import com.tici.vpn.proxy.master.utils.textChanges
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/** User-editable ad-block rule list, reached from Settings. Swapped inside `MainActivity`. */
@AndroidEntryPoint
class AdsBlockFragment : BaseFragment<FragmentAdsBlockBinding>() {

    private val viewModel: AdsBlockViewModel by viewModels()
    private val adapter = AdRuleAdapter()

    /** SAF picker: works on every API level without storage permissions. */
    private val exportCaLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument(CA_MIME_TYPE)) { uri ->
            uri?.let { saveCaCertificate(it) }
        }

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentAdsBlockBinding {
        return FragmentAdsBlockBinding.inflate(inflater, container, false)
    }

    override val screenType: ScreenType
        get() = AppScreenType.AdsBlockFragment

    override fun initViews(savedInstanceState: Bundle?) {
        adapter.onToggle = { viewModel.toggle(it) }
        adapter.onDelete = { rule ->
            viewModel.delete(rule)
            Snackbar.make(binding.root, R.string.ads_rules_deleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.ads_rules_undo) { viewModel.undoDelete(rule) }
                .show()
        }

        binding.apply {
            recyclerViewRules.adapter = adapter

            btnAddRule.setOnClickListener { submitAdd() }
            edtAddRule.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) submitAdd()
                actionId == EditorInfo.IME_ACTION_DONE
            }
            edtSearch.textChanges(lifecycleScope) { adapter.filter(it) }

            btnReset.setOnClickListener { confirmReset() }
            tvExportCa.setOnClickListener {
                exportCaLauncher.launch(getString(R.string.ads_rules_export_ca_file))
            }

            bannerMasterOff.setOnClickListener {
                hideKeyboard()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            bannerQuarantine.setOnClickListener {
                viewModel.reEnableRules()
                bannerQuarantine.setVisible(false)
            }
        }

        viewModel.rules.observe(viewLifecycleOwner) { adapter.setData(it) }
        viewModel.isLoading.observe(viewLifecycleOwner) { binding.progress.setVisible(it) }
        viewModel.addResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AdsBlockViewModel.AddResult.Added -> {
                    binding.edtAddRule.setText("")
                    binding.tvAddError.setVisible(false)
                }
                is AdsBlockViewModel.AddResult.Error -> {
                    binding.tvAddError.setText(result.message)
                    binding.tvAddError.setVisible(true)
                }
                null -> return@observe
            }
            viewModel.consumeAddResult()
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    /** Fragments are shown/hidden rather than resumed when swapped, so refresh on show too. */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) refresh()
    }

    private fun refresh() {
        binding.bannerMasterOff.setVisible(!BaseAppConfig.adsBlock)
        binding.bannerQuarantine.setVisible(viewModel.isQuarantined)
        viewModel.load()
    }

    private fun submitAdd() {
        viewModel.addRule(binding.edtAddRule.text?.toString().orEmpty())
    }

    private fun confirmReset() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.ads_rules_reset)
            .setMessage(R.string.ads_rules_reset_confirm)
            .setPositiveButton(R.string.ads_rules_reset) { _, _ -> viewModel.resetToDefaults() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun saveCaCertificate(uri: Uri) {
        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            val saved = withContext(Dispatchers.IO) {
                try {
                    val pem = MitmCaStore.certificatePem(appContext)
                    appContext.contentResolver.openOutputStream(uri)?.use {
                        it.write(pem.toByteArray(Charsets.US_ASCII))
                    } != null
                } catch (e: Exception) {
                    Timber.e(e, "Failed to export MITM CA certificate")
                    false
                }
            }
            if (saved) {
                showCaInstallInstructions()
            } else {
                Snackbar.make(binding.root, R.string.ads_rules_ca_save_failed, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    /** Android 11+ forbids apps from installing CA certs; the user must do it in Settings. */
    private fun showCaInstallInstructions() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.ads_rules_ca_saved_title)
            .setMessage(R.string.ads_rules_ca_saved_message)
            .setPositiveButton(R.string.ads_rules_ca_open_settings) { _, _ ->
                try {
                    startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
            .setNegativeButton(android.R.string.ok, null)
            .show()
    }

    fun hideKeyboard() {
        binding.edtAddRule.hideKeyboard()
        binding.edtSearch.hideKeyboard()
    }

    private companion object {
        const val CA_MIME_TYPE = "application/x-x509-ca-cert"
    }
}

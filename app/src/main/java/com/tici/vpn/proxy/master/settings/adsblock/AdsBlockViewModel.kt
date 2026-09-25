package com.tici.vpn.proxy.master.settings.adsblock

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tici.vpn.proxy.master.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/** Rule list screen. Free feature — no VIP gate. Repository calls are main-safe (IO inside). */
@HiltViewModel
class AdsBlockViewModel @Inject constructor(
    private val repository: AdsBlockInterface,
    private val crashGuard: AdRulesCrashGuard,
    private val reapplier: AdsRuleReapplier
) : ViewModel() {

    sealed class AddResult {
        data object Added : AddResult()
        data class Error(@StringRes val message: Int) : AddResult()
    }

    private val _rules = MutableLiveData<List<AdRuleUI>>()
    val rules: LiveData<List<AdRuleUI>> = _rules

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _addResult = MutableLiveData<AddResult?>()
    val addResult: LiveData<AddResult?> = _addResult

    val isQuarantined: Boolean get() = crashGuard.isQuarantined

    fun load() = launchSafely {
        _isLoading.value = true
        try {
            _rules.value = repository.getAll()
        } finally {
            _isLoading.value = false
        }
    }

    /** Gate 1: [DomainValidator] is the only place rules are defined — the UI never re-implements them. */
    fun addRule(raw: String) = launchSafely {
        val error = when (val v = DomainValidator.validate(raw)) {
            is DomainValidator.Result.Invalid -> v.reason.message()
            is DomainValidator.Result.Valid -> when {
                repository.count() >= MAX_RULES -> R.string.ads_rules_error_cap
                !repository.addRule(v.domain, raw.trim()) -> R.string.ads_rules_error_duplicate
                else -> null
            }
        }
        _addResult.value = if (error == null) AddResult.Added else AddResult.Error(error)
        if (error == null) {
            reapplier.notifyRulesChanged()
            load()
        }
    }

    fun consumeAddResult() {
        _addResult.value = null
    }

    fun toggle(rule: AdRuleUI) = launchSafely {
        repository.setEnabled(rule.id, rule.enabled)
        reapplier.notifyRulesChanged()
    }

    fun delete(rule: AdRuleUI) = launchSafely {
        repository.delete(rule.id)
        reapplier.notifyRulesChanged()
        load()
    }

    fun undoDelete(rule: AdRuleUI) = launchSafely {
        repository.restoreRule(rule)
        reapplier.notifyRulesChanged()
        load()
    }

    fun resetToDefaults() = launchSafely {
        repository.resetToDefaults()
        reapplier.notifyRulesChanged()
        load()
    }

    /** Quarantine banner tap: try the user's rules again (applied live if connected). */
    fun reEnableRules() {
        crashGuard.clearQuarantine()
        reapplier.notifyRulesChanged()
    }

    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                Timber.e(e, "Ad-block rule operation failed")
            }
        }
    }

    private fun DomainValidator.Reason.message(): Int = when (this) {
        DomainValidator.Reason.EMPTY -> R.string.ads_rules_error_empty
        DomainValidator.Reason.WILDCARD -> R.string.ads_rules_error_wildcard
        DomainValidator.Reason.TOO_LONG -> R.string.ads_rules_error_too_long
        DomainValidator.Reason.INVALID_FORMAT -> R.string.ads_rules_error_format
    }

    companion object {
        /** Assumed until phase-01's throughput spike measures a real number. */
        const val MAX_RULES = 2000
    }
}

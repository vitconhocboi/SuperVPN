package com.tici.vpn.proxy.master.settings.adsblock

import android.content.Context
import com.tici.vpn.proxy.master.network.EngineBlocklistFile
import com.tici.vpn.proxy.master.network.LocalVpnService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies rule edits to the running netstack engine without reconnecting the VPN.
 */
@Singleton
class AdsRuleReapplier @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: AdsBlockInterface
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val rulesChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        @OptIn(FlowPreview::class)
        rulesChanged
            .debounce(DEBOUNCE_MS)
            .onEach { reapply() }
            .launchIn(scope)
    }

    /** Call after every persisted rule change and after the master switch flips. */
    fun notifyRulesChanged() {
        rulesChanged.tryEmit(Unit)
    }

    private suspend fun reapply() {
        if (LocalVpnService.Instance?.isFilterRunning != true) return
        try {
            val file = EngineBlocklistFile.write(context, repository.renderBlocklist())
            engine.Engine.reloadBlocklist(file.absolutePath)
            Timber.d("Ad-block rules reapplied in netstack without reconnect")
        } catch (e: Exception) {
            Timber.e(e, "Failed to reapply ad-block rules")
        }
    }

    private companion object {
        const val DEBOUNCE_MS = 1000L
    }
}

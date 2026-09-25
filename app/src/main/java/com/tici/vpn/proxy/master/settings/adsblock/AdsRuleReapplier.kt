package com.tici.vpn.proxy.master.settings.adsblock

import android.content.Context
import com.tici.vpn.proxy.master.network.LocalVpnService
import com.tici.vpn.proxy.master.network.PrivoxyActionFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies rule edits to a running filter without reconnecting the VPN.
 *
 * Privoxy reloads `default.action` on the next accepted connection once its mtime changes, so
 * rewriting the file is enough: the tunnel, session timer and notification are untouched.
 * Edits are debounced (a burst of toggles → one rewrite) on an app-wide scope, so leaving the
 * screen mid-burst does not lose the change. While disconnected this is a no-op — the next
 * connect renders the file fresh.
 */
@Singleton
class AdsRuleReapplier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AdsBlockInterface,
    private val crashGuard: AdRulesCrashGuard
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
        // Skip while disconnected or while a start is still in flight — that start renders
        // the file itself and owns the breadcrumb.
        if (LocalVpnService.Instance?.isFilterRunning != true) return
        try {
            val body = repository.renderActionFile()
            // Same breadcrumb as a start: if this file kills Privoxy (exit(1) takes the whole
            // process), the next launch sees the pending flag and quarantines the rules.
            crashGuard.markStartPending()
            PrivoxyActionFile.write(PrivoxyActionFile.file(context), body)
            triggerReload()
            delay(PARSE_GRACE_MS)
            crashGuard.clearStartPending()
            Timber.d("Ad-block rules reapplied without reconnect")
        } catch (e: Exception) {
            crashGuard.clearStartPending()
            Timber.e(e, "Failed to reapply ad-block rules")
        }
    }

    /**
     * Privoxy only runs its loaders when it accepts a connection. Poke it now so the parse (and
     * any fatal error) happens inside our breadcrumb window, not at some random later request.
     */
    private fun triggerReload() {
        try {
            Socket().use { it.connect(InetSocketAddress(FILTER_HOST, FILTER_PORT), CONNECT_TIMEOUT_MS) }
        } catch (e: Exception) {
            Timber.w(e, "Could not poke local filter; rules load on the next request")
        }
    }

    private companion object {
        const val DEBOUNCE_MS = 1000L
        const val PARSE_GRACE_MS = 500L
        const val CONNECT_TIMEOUT_MS = 1000

        // Mirrors VpnManager's listen-address.
        const val FILTER_HOST = "127.0.0.1"
        const val FILTER_PORT = 8118
    }
}

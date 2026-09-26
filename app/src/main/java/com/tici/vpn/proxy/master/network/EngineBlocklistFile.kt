package com.tici.vpn.proxy.master.network

import android.content.Context
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * The one place that writes the netstack engine's blocklist file, shared by VPN start
 * ([LocalVpnService]) and live rule reloads (`AdsRuleReapplier`). Body comes from
 * `AdsBlockInterface.renderBlocklist()`.
 */
object EngineBlocklistFile {

    fun file(context: Context): File = File(context.filesDir, "blocklist.txt")

    /** Temp file + atomic rename, so the engine never loads a half-written file. */
    @Synchronized
    fun write(context: Context, text: String): File {
        val target = file(context)
        val tmp = File(target.parentFile, "${target.name}.tmp")
        tmp.writeText(text)
        try {
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            tmp.delete()
            throw e
        }
        return target
    }
}

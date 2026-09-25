package com.tici.vpn.proxy.master.network

import android.content.Context
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * The one place that writes Privoxy's `default.action`, shared by the initial start
 * ([VpnManager]) and live rule reloads (`AdsRuleReapplier`).
 *
 * Privoxy re-parses the file on the next accepted connection whenever its mtime changes
 * (`jcc.c` → `run_loader` → `load_action_files` → `check_file_changed`), so no restart is needed.
 */
object PrivoxyActionFile {

    fun file(context: Context): File = File(File(context.filesDir, "privoxy"), "default.action")

    /**
     * Temp file + rename, so Privoxy never parses a half-written file if the process dies
     * mid-write. The mtime is forced strictly past the previous one: Privoxy compares whole
     * seconds, so two writes within the same second would otherwise go unnoticed.
     */
    @Synchronized
    fun write(target: File, text: String) {
        target.parentFile?.mkdirs()
        val previousMtime = if (target.exists()) target.lastModified() else 0L
        val tmp = File(target.parentFile, "${target.name}.tmp")
        tmp.writeText(text)
        try {
            // ATOMIC_MOVE replaces the target in one step (rename(2) on Android). Plain
            // File.renameTo is not guaranteed to overwrite an existing file on every platform.
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            tmp.delete()
            throw e
        }
        val minMtime = (previousMtime / 1000 + 1) * 1000
        if (target.lastModified() < minMtime) target.setLastModified(minMtime)
    }
}

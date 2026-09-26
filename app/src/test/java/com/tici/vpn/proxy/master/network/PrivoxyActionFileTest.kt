package com.tici.vpn.proxy.master.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PrivoxyActionFileTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun target() = File(tmp.root, "privoxy/default.action")

    @Test
    fun writesContentAndCreatesParentDir() {
        val f = target()
        PrivoxyActionFile.write(f, "hello\n")
        assertEquals("hello\n", f.readText())
    }

    @Test
    fun replacesExistingContentAndLeavesNoTempFile() {
        val f = target()
        PrivoxyActionFile.write(f, "old")
        PrivoxyActionFile.write(f, "new")
        assertEquals("new", f.readText())
        assertFalse(File(f.parentFile, "default.action.tmp").exists())
    }

    @Test
    fun emptyBodyWritesEmptyFile() {
        val f = target()
        PrivoxyActionFile.write(f, "x")
        PrivoxyActionFile.write(f, "")
        assertEquals("", f.readText())
    }

    /** Privoxy compares st_mtime in whole seconds; back-to-back writes must still look changed. */
    @Test
    fun rapidRewritesAlwaysAdvanceMtimeByWholeSeconds() {
        val f = target()
        PrivoxyActionFile.write(f, "a")
        var previousSecond = f.lastModified() / 1000
        repeat(5) { i ->
            PrivoxyActionFile.write(f, "b$i")
            val second = f.lastModified() / 1000
            assertTrue("write $i: mtime second $second not after $previousSecond", second > previousSecond)
            previousSecond = second
        }
    }

    @Test
    fun mtimeInThePastIsNotMovedBackwards() {
        val f = target()
        PrivoxyActionFile.write(f, "a")
        f.setLastModified(1_000_000_000_000L) // 2001
        PrivoxyActionFile.write(f, "b")
        assertTrue(f.lastModified() / 1000 > 1_000_000_000L)
    }
}

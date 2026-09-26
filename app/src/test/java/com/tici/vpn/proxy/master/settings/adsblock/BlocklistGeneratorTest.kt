package com.tici.vpn.proxy.master.settings.adsblock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlocklistGeneratorTest {

    @Test
    fun emptyListRendersEmptyFile() {
        assertEquals(BlocklistGenerator.RenderResult("", 0), BlocklistGenerator.render(emptyList()))
    }

    @Test
    fun onlyInvalidRowsRenderEmptyFileWithoutDanglingHeader() {
        val r = BlocklistGenerator.render(listOf("*.bad.com", "nope"))
        assertEquals("", r.text)
        assertEquals(2, r.droppedCount)
    }

    @Test
    fun threeValidDomainsRenderExactText() {
        val r = BlocklistGenerator.render(
            listOf("doubleclick.net", "pubmatic.com", "googlesyndication.com")
        )
        val expected = BlocklistGenerator.HEADER +
            "doubleclick.net\n" +
            "pubmatic.com\n" +
            "googlesyndication.com\n"
        assertEquals(expected, r.text)
        assertEquals(0, r.droppedCount)
    }

    @Test
    fun invalidRowIsDroppedAndCountedOthersIntact() {
        val r = BlocklistGenerator.render(listOf("a.com", "ev{il}.com", "b.com"))
        assertEquals(1, r.droppedCount)
        assertEquals(BlocklistGenerator.HEADER + "a.com\nb.com\n", r.text)
    }

    @Test
    fun duplicatesAfterCanonicalisationEmittedOnce() {
        val r = BlocklistGenerator.render(listOf("a.com", ".A.com", "https://a.com/", "A.com/x", "a.com/x?q"))
        assertEquals(BlocklistGenerator.HEADER + "a.com\na.com/x\n", r.text)
        assertEquals(0, r.droppedCount)
    }

    @Test
    fun bodyNeverContainsMetacharacters() {
        val hostile = listOf(
            "a.com", "*.x.com", "x?.com", "[a].com", "{x}.com", "}+block{x}.com", "#x.com",
            "x\\y.com", "a/b.com", "x.com:abc", "x.com\n{+block{}}", "x\u0000.com", "müller.de",
            "https://ok.example.org/path?q=1#frag", "  .Trailing.COM  ", "127.0.0.1"
        )
        val body = BlocklistGenerator.render(hostile).text.removePrefix(BlocklistGenerator.HEADER)
        // One rule per line; hosts are canonical, paths are URL-path characters only.
        assertFalse("whitespace inside a rule:\n$body", Regex("[ \\t\\r\u0000]").containsMatchIn(body))
        val line = Regex("^[a-z0-9.-]+(/[A-Za-z0-9._~!$&'()+,;=:@%/-]*)?$")
        body.lineSequence().filter { it.isNotEmpty() }.forEach {
            assertTrue("line not in domain[/path] form: '$it'", line.matches(it))
            assertFalse("rule looks like a comment or exception: '$it'", it.startsWith("#") || it.startsWith("@@"))
        }
    }

    @Test
    fun pathRuleKeepsPathCaseAndDropsQuery() {
        val r = BlocklistGenerator.render(listOf("https://ADS.Example.com/Banner/X.png?id=1#top"))
        assertEquals(BlocklistGenerator.HEADER + "ads.example.com/Banner/X.png\n", r.text)
    }
}

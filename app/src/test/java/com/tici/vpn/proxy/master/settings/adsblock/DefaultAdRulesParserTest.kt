package com.tici.vpn.proxy.master.settings.adsblock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DefaultAdRulesParserTest {

    private fun parse(vararg lines: String) = DefaultAdRulesParser.parse(lines.asSequence())

    @Test
    fun keepsPlainDomainsInFileOrder() {
        assertEquals(listOf("b.com", "a.com"), parse("b.com", "a.com"))
    }

    @Test
    fun dropsBlankLinesAndComments() {
        assertEquals(listOf("a.com"), parse("", "   ", "# comment", "  # indented", "a.com"))
    }

    @Test
    fun trimsAndLowercases() {
        assertEquals(listOf("ads.example.com"), parse("  ADS.Example.COM\t"))
    }

    @Test
    fun stripsLeadingDots() {
        assertEquals(listOf("doubleclick.net"), parse(".doubleclick.net"))
        assertEquals(listOf("doubleclick.net"), parse("..doubleclick.net"))
    }

    @Test
    fun dedupesAfterCanonicalisationFirstOccurrenceWins() {
        assertEquals(
            listOf("doubleclick.net", "adnxs.com"),
            parse("doubleclick.net", "adnxs.com", ".DoubleClick.net", " doubleclick.net ")
        )
    }

    @Test
    fun loneDotsCollapseToEmptyAndAreDropped() {
        assertEquals(emptyList<String>(), parse(".", "..."))
    }

    @Test
    fun emptyInputYieldsEmptyList() {
        assertEquals(emptyList<String>(), parse())
    }

    @Test
    fun canonicalizeStripsCarriageReturn() {
        assertEquals("a.com", DefaultAdRulesParser.canonicalize("a.com\r"))
    }

    @Test
    fun canonicalizeLowercasesHostButKeepsPathCase() {
        assertEquals("ads.example.com/Banner/X.png", DefaultAdRulesParser.canonicalize(" .ADS.Example.com/Banner/X.png "))
    }

    @Test
    fun preservesTrailingDotsForValidatorToReject() {
        // Trailing dots are not stripped by the parser (only leading dots are).
        // This allows the validator in phase-03 to catch and reject malformed domains.
        // Real asset files must not contain trailing dots, but the parser gracefully
        // accepts them and lets the validator enforce the canonical form.
        assertEquals(listOf("example.com.", "a.co.uk."), parse("example.com.", "a.co.uk."))
    }

    @Test
    fun shippedAssetParsesToCanonicalGlobFreeDomains() {
        // Gradle runs unit tests with the module dir (app/) as working directory.
        val asset = File("src/main/assets/default_ad_rules.txt")
        val domains = asset.bufferedReader().useLines { DefaultAdRulesParser.parse(it) }

        assertTrue("asset must not be empty", domains.isNotEmpty())
        assertEquals("asset must not contain duplicates", domains.size, domains.toSet().size)
        val canonical = Regex("^[a-z0-9-]+(\\.[a-z0-9-]+)+$")
        domains.forEach {
            assertTrue("not canonical or contains a glob: '$it'", canonical.matches(it))
        }
    }
}

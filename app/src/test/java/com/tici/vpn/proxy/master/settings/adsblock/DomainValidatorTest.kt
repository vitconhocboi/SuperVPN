package com.tici.vpn.proxy.master.settings.adsblock

import com.tici.vpn.proxy.master.settings.adsblock.DomainValidator.Reason
import com.tici.vpn.proxy.master.settings.adsblock.DomainValidator.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainValidatorTest {

    private fun assertValid(input: String, expected: String) {
        assertEquals("input: '$input'", Result.Valid(expected), DomainValidator.validate(input))
    }

    private fun assertInvalid(input: String, reason: Reason? = null) {
        val r = DomainValidator.validate(input)
        assertTrue("expected invalid for '$input' but was $r", r is Result.Invalid)
        if (reason != null) assertEquals(Result.Invalid(reason), r)
    }

    // --- accept ---

    @Test fun acceptsPlainDomain() = assertValid("example.com", "example.com")
    @Test fun stripsLeadingDot() = assertValid(".example.com", "example.com")
    @Test fun stripsSchemeAndQueryKeepsPath() = assertValid("https://ads.example.com/foo?x=1", "ads.example.com/foo")
    @Test fun stripsFragment() = assertValid("ads.example.com/a#frag", "ads.example.com/a")
    @Test fun bareSlashMeansWholeDomain() = assertValid("ads.example.com/", "ads.example.com")
    @Test fun pathKeepsCaseHostLowercased() = assertValid("ADS.Example.com/Banner/A.png", "ads.example.com/Banner/A.png")
    @Test fun portStrippedBeforePath() = assertValid("ads.example.com:443/ad", "ads.example.com/ad")
    @Test fun acceptsPercentEncodedPath() = assertValid("x.com/a%20b/c", "x.com/a%20b/c")
    @Test fun stripsHttpScheme() = assertValid("http://ads.example.com", "ads.example.com")
    @Test fun stripsPort() = assertValid("ads.example.com:8080", "ads.example.com")
    @Test fun lowercases() = assertValid("EXAMPLE.COM", "example.com")
    @Test fun trimsWhitespaceAndQuotes() = assertValid("  \"example.com\"  ", "example.com")
    @Test fun acceptsPunycode() = assertValid("xn--mller-kva.de", "xn--mller-kva.de")
    @Test fun convertsIdnToPunycode() = assertValid("müller.de", "xn--mller-kva.de")
    @Test fun acceptsPunycodeTld() = assertValid("example.xn--p1ai", "example.xn--p1ai")
    @Test fun acceptsInnerHyphenAndDigits() = assertValid("ad-1.cdn2.example.com", "ad-1.cdn2.example.com")
    @Test fun accepts63CharLabel() = "a".repeat(63).let { assertValid("$it.com", "$it.com") }

    @Test
    fun acceptsLongHostUnderLimit() {
        val host = List(4) { "a".repeat(61) }.joinToString(".") + ".com" // 4*61 + 3 + 4 = 251
        assertValid(host, host)
    }

    // --- reject ---

    @Test fun rejectsEmpty() = assertInvalid("", Reason.EMPTY)
    @Test fun rejectsBlank() = assertInvalid("   ", Reason.EMPTY)
    @Test fun rejectsSingleLabel() = assertInvalid("example", Reason.INVALID_FORMAT)
    @Test fun rejectsBareTld() = assertInvalid(".com", Reason.INVALID_FORMAT)
    @Test fun rejectsLeadingHyphen() = assertInvalid("-bad.com", Reason.INVALID_FORMAT)
    @Test fun rejectsTrailingHyphen() = assertInvalid("bad-.com", Reason.INVALID_FORMAT)
    @Test fun rejectsSpace() = assertInvalid("ex ample.com")
    @Test fun rejectsHash() = assertInvalid("ex#ample.com")
    @Test fun rejectsBraces() = assertInvalid("ex{ample}.com")
    @Test fun rejectsWildcard() = assertInvalid("*.example.com", Reason.WILDCARD)
    @Test fun rejectsInnerWildcard() = assertInvalid("ads*.example.com", Reason.WILDCARD)
    @Test fun rejectsCharClass() = assertInvalid("[a-z].com")
    @Test fun rejectsPathBeforeDomain() = assertInvalid("a/b.com")
    @Test fun rejectsIpv4() = assertInvalid("127.0.0.1")
    @Test fun rejectsNumericTld() = assertInvalid("example.123")
    @Test fun rejectsEmbeddedNewline() = assertInvalid("ads.example.com\nevil.com")
    @Test fun rejectsNulByte() = assertInvalid("ads.example\u0000.com")
    @Test fun rejectsBackslash() = assertInvalid("ads\\example.com")
    @Test fun rejectsQuestionMark() = assertInvalid("ads?.example.com")
    @Test fun rejectsDoubleDot() = assertInvalid("ads..example.com")
    @Test fun rejectsTrailingDot() = assertInvalid("example.com.")
    @Test fun rejectsNonNumericPort() = assertInvalid("example.com:abc")
    @Test fun rejectsUnderscore() = assertInvalid("ad_server.example.com")
    @Test fun rejectsSpaceInPath() = assertInvalid("x.com/a b", Reason.INVALID_FORMAT)
    @Test fun rejectsNewlineInPath() = assertInvalid("x.com/a\nevil.com", Reason.INVALID_FORMAT)
    @Test fun rejectsWildcardInPath() = assertInvalid("x.com/ads/*", Reason.WILDCARD)
    @Test fun rejectsBackslashInPath() = assertInvalid("x.com/a\\b", Reason.INVALID_FORMAT)
    @Test fun rejectsOverlongPath() = assertInvalid("x.com/" + "a".repeat(512), Reason.TOO_LONG)
    @Test fun rejects64CharLabel() = assertInvalid("a".repeat(64) + ".com")

    /**
     * IDN normalisation may legitimately rewrite these (zero-width chars dropped, fullwidth dots
     * mapped, other scripts → punycode). The safety invariant is only that nothing outside
     * `[a-z0-9.-]` can ever come out as Valid.
     */
    @Test
    fun unicodeTricksNeverYieldNonAsciiOutput() {
        val tricky = listOf(
            "exam‍ple.com", "exam​ple.com", "exam﻿ple.com", "exam‏ple.com",
            "exam‎ple.com", "exam؜ple.com", "example。com", "example．com",
            "exampĺe.com", "αlpha.com", "cyrillicа.com", "tëstа.com",
            "ａｄｓ.com", "ads.example.com x", "‮example.com"
        )
        val safe = Regex("^[a-z0-9.-]+(/[A-Za-z0-9._~!$&'()+,;=:@%/-]*)?$")
        tricky.forEach {
            val r = DomainValidator.validate(it)
            if (r is Result.Valid) assertTrue("unsafe output for '$it': ${r.domain}", safe.matches(r.domain))
        }
    }

    @Test
    fun rejects254CharHost() {
        val host = List(4) { "a".repeat(61) }.joinToString(".") + ".comx" + "a".repeat(2) // 254
        assertEquals(254, host.length)
        assertInvalid(host, Reason.TOO_LONG)
    }
}

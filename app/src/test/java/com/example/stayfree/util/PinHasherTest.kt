package com.example.stayfree.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class PinHasherTest {

    @Test
    fun `roundtrip - correct PIN verifies`() {
        val stored = PinHasher.hash("1234")
        assertTrue(PinHasher.verify("1234", stored))
    }

    @Test
    fun `wrong PIN is rejected`() {
        val stored = PinHasher.hash("1234")
        assertFalse(PinHasher.verify("4321", stored))
        assertFalse(PinHasher.verify("", stored))
        assertFalse(PinHasher.verify("12345", stored))
    }

    @Test
    fun `same PIN produces different hashes (random salt)`() {
        assertNotEquals(PinHasher.hash("1234"), PinHasher.hash("1234"))
    }

    @Test
    fun `stored format is self-describing`() {
        val stored = PinHasher.hash("1234")
        assertTrue(PinHasher.isModernFormat(stored))
        val parts = stored.split('$')
        assertTrue(parts.size == 4)
        assertTrue(parts[0].startsWith("pbkdf2-"))
        assertTrue(parts[1].toInt() >= 200_000)
    }

    @Test
    fun `legacy unsalted sha256 hash is rejected, not accepted as a match`() {
        val legacy = MessageDigest.getInstance("SHA-256")
            .digest("1234".toByteArray())
            .joinToString("") { "%02x".format(it) }
        assertFalse(PinHasher.isModernFormat(legacy))
        assertFalse(PinHasher.verify("1234", legacy))
    }

    @Test
    fun `malformed stored values are rejected`() {
        assertFalse(PinHasher.verify("1234", ""))
        assertFalse(PinHasher.verify("1234", "pbkdf2-sha256\$abc\$zz\$zz"))
        assertFalse(PinHasher.verify("1234", "pbkdf2-unknown\$200000\$00\$00"))
        assertFalse(PinHasher.isModernFormat(null))
    }
}

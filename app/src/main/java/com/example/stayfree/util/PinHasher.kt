package com.example.stayfree.util

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Salted PBKDF2 PIN hashing. Stored format:
 * `<algId>$<iterations>$<saltHex>$<hashHex>`, e.g. `pbkdf2-sha256$200000$ab..$cd..`.
 *
 * PBKDF2WithHmacSHA256 needs API 26; older devices fall back to the SHA1 KDF
 * (still salted + stretched). The algorithm is part of the stored string so
 * verification always uses whatever the hash was created with.
 */
object PinHasher {

    private const val ITERATIONS = 200_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16
    private const val PREFIX = "pbkdf2-"

    private enum class Algorithm(val id: String, val jcaName: String) {
        SHA256("pbkdf2-sha256", "PBKDF2WithHmacSHA256"),
        SHA1("pbkdf2-sha1", "PBKDF2WithHmacSHA1");

        companion object {
            fun fromId(id: String): Algorithm? = entries.firstOrNull { it.id == id }
        }
    }

    fun hash(pin: String): String {
        val algorithm = preferredAlgorithm()
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = pbkdf2(algorithm, pin, salt, ITERATIONS)
        return "${algorithm.id}\$$ITERATIONS\$${salt.toHex()}\$${hash.toHex()}"
    }

    fun verify(pin: String, stored: String): Boolean {
        val parts = stored.split('$')
        if (parts.size != 4) return false
        val algorithm = Algorithm.fromId(parts[0]) ?: return false
        val iterations = parts[1].toIntOrNull()?.takeIf { it > 0 } ?: return false
        val salt = parts[2].hexToBytes() ?: return false
        val expected = parts[3].hexToBytes() ?: return false
        val actual = try {
            pbkdf2(algorithm, pin, salt, iterations)
        } catch (e: NoSuchAlgorithmException) {
            return false
        }
        // Constant-time comparison.
        return MessageDigest.isEqual(expected, actual)
    }

    /** True for hashes produced by this class; legacy plain SHA-256 hashes return false. */
    fun isModernFormat(stored: String?): Boolean = stored?.startsWith(PREFIX) == true

    private fun preferredAlgorithm(): Algorithm = try {
        SecretKeyFactory.getInstance(Algorithm.SHA256.jcaName)
        Algorithm.SHA256
    } catch (e: NoSuchAlgorithmException) {
        Algorithm.SHA1
    }

    private fun pbkdf2(algorithm: Algorithm, pin: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return SecretKeyFactory.getInstance(algorithm.jcaName).generateSecret(spec).encoded
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray? {
        if (length % 2 != 0 || isEmpty()) return null
        val out = ByteArray(length / 2)
        for (i in out.indices) {
            val hi = Character.digit(this[i * 2], 16)
            val lo = Character.digit(this[i * 2 + 1], 16)
            if (hi < 0 || lo < 0) return null
            out[i] = ((hi shl 4) or lo).toByte()
        }
        return out
    }
}

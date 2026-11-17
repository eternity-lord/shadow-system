package com.shadowsystem.core

import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Backup encryption: PBKDF2-HMAC-SHA256 key derivation + AES-256-GCM.
 * File layout: "SSB1" | salt(16) | iv(12) | ciphertext+tag.
 */
object Crypto {
    private const val MAGIC = "SSB1"
    private const val SALT_LEN = 16
    private const val IV_LEN = 12
    private const val BACKUP_ITERATIONS = 120_000
    private const val PIN_ITERATIONS = 60_000
    private val random = SecureRandom()

    fun newSalt(): ByteArray = ByteArray(SALT_LEN).also { random.nextBytes(it) }

    fun deriveKey(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    fun encrypt(plain: ByteArray, password: CharArray): ByteArray {
        val salt = newSalt()
        val iv = ByteArray(IV_LEN).also { random.nextBytes(it) }
        val key = SecretKeySpec(deriveKey(password, salt, BACKUP_ITERATIONS), "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val body = cipher.doFinal(plain)
        return MAGIC.toByteArray(Charsets.US_ASCII) + salt + iv + body
    }

    fun decrypt(data: ByteArray, password: CharArray): ByteArray {
        val headerLen = MAGIC.length + SALT_LEN + IV_LEN
        require(data.size > headerLen && String(data, 0, MAGIC.length, Charsets.US_ASCII) == MAGIC) {
            "Not a Shadow System backup"
        }
        val salt = data.copyOfRange(MAGIC.length, MAGIC.length + SALT_LEN)
        val iv = data.copyOfRange(MAGIC.length + SALT_LEN, headerLen)
        val body = data.copyOfRange(headerLen, data.size)
        val key = SecretKeySpec(deriveKey(password, salt, BACKUP_ITERATIONS), "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return try {
            cipher.doFinal(body)
        } catch (e: AEADBadTagException) {
            throw IllegalArgumentException("Wrong password or corrupted backup", e)
        }
    }

    fun hashPin(pin: String, salt: ByteArray): String =
        toHex(deriveKey(pin.toCharArray(), salt, PIN_ITERATIONS))

    fun toHex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

    fun fromHex(hex: String): ByteArray =
        ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}

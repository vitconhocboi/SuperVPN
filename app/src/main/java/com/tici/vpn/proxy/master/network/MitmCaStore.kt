package com.tici.vpn.proxy.master.network

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import timber.log.Timber
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Persists the selective-MITM root CA (engine/ca.go) so the certificate the user installed keeps
 * matching across restarts.
 *
 * The CA private key must be exportable (Go signs leaf certs with it), so it cannot live inside
 * Android Keystore itself. Instead it is encrypted with a non-exportable Keystore AES-GCM key and
 * stored in `noBackupFilesDir`; the certificate is public and stored as plain PEM.
 * If the Keystore key is lost (e.g. restored device), a fresh CA is generated and the user must
 * re-install the certificate.
 */
object MitmCaStore {

    private const val KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "supervpn_mitm_ca_wrap"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_BITS = 128
    private const val CERT_FILE = "mitm_ca.crt"
    private const val KEY_FILE = "mitm_ca.key.enc"

    class Pem(val certPem: String, val keyPem: String)

    /** Loads the persisted CA, generating and persisting one on first use. Blocking I/O. */
    @Synchronized
    fun loadOrCreate(context: Context): Pem {
        val dir = context.noBackupFilesDir
        val certFile = File(dir, CERT_FILE)
        val keyFile = File(dir, KEY_FILE)
        if (certFile.exists() && keyFile.exists()) {
            try {
                return Pem(certFile.readText(), decrypt(keyFile.readBytes()))
            } catch (e: Exception) {
                Timber.w(e, "MITM CA unreadable; generating a new one (certificate must be re-installed)")
            }
        }
        val generated = engine.Engine.generateCA()
        val pem = Pem(generated.certPEM, generated.keyPEM)
        keyFile.writeBytes(encrypt(pem.keyPem))
        certFile.writeText(pem.certPem)
        return pem
    }

    /** Public root certificate for the user to install. Blocking I/O. */
    fun certificatePem(context: Context): String = loadOrCreate(context).certPem

    private fun wrapKey(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (ks.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        gen.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return gen.generateKey()
    }

    /** Output: 1-byte IV length, IV, ciphertext+tag. */
    private fun encrypt(plain: String): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, wrapKey()) }
        val iv = cipher.iv
        return byteArrayOf(iv.size.toByte()) + iv + cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
    }

    private fun decrypt(blob: ByteArray): String {
        val ivLen = blob[0].toInt()
        require(ivLen in 12..16 && blob.size > 1 + ivLen) { "corrupt MITM CA key file" }
        val iv = blob.copyOfRange(1, 1 + ivLen)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, wrapKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        }
        return String(cipher.doFinal(blob, 1 + ivLen, blob.size - 1 - ivLen), Charsets.UTF_8)
    }
}

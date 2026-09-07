package com.mindlizzard.feetstudio.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureKeyStore(private val context: Context) {
    private val prefs =
        context.getSharedPreferences(
            "feet_studio_secure",
            Context.MODE_PRIVATE
        )

    private val alias = "feet-studio-gemini-key"

    private fun getOrCreateKey(): SecretKey {
        val keyStore =
            KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }

        (keyStore.getKey(alias, null) as? SecretKey)?.let {
            return it
        }

        val generator =
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )

        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .build()
        )

        return generator.generateKey()
    }

    fun saveApiKey(value: String) {
        val clean = value.trim()

        if (clean.isBlank()) {
            clear()
            return
        }

        require('\n' !in clean && '\r' !in clean) {
            "Plak alleen de Gemini API-key, op één regel."
        }

        require(clean.length in 20..200) {
            "Deze tekst lijkt geen geldige Gemini API-key."
        }

        require(
            clean.all {
                it.isLetterOrDigit() ||
                    it == '_' ||
                    it == '-'
            }
        ) {
            "API-key bevat ongeldige tekens. Plak alleen de key zelf."
        }

        val cipher =
            Cipher.getInstance("AES/GCM/NoPadding")

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateKey()
        )

        val encrypted =
            cipher.doFinal(
                clean.toByteArray(Charsets.UTF_8)
            )

        prefs.edit()
            .putString(
                "cipher",
                Base64.encodeToString(
                    encrypted,
                    Base64.NO_WRAP
                )
            )
            .putString(
                "iv",
                Base64.encodeToString(
                    cipher.iv,
                    Base64.NO_WRAP
                )
            )
            .apply()
    }

    fun loadApiKey(): String {
        val encrypted =
            prefs.getString("cipher", null)
                ?: return ""

        val iv =
            prefs.getString("iv", null)
                ?: return ""

        return runCatching {
            val cipher =
                Cipher.getInstance("AES/GCM/NoPadding")

            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(
                    128,
                    Base64.decode(
                        iv,
                        Base64.NO_WRAP
                    )
                )
            )

            String(
                cipher.doFinal(
                    Base64.decode(
                        encrypted,
                        Base64.NO_WRAP
                    )
                ),
                Charsets.UTF_8
            )
        }.getOrDefault("")
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}

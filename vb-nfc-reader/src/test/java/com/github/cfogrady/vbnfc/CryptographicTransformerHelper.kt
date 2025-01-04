package com.github.cfogrady.vbnfc

import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

// This class allows for creating new test keys
class CryptographicTransformerHelper {

    companion object {
        fun generateAesKey(): String {
            val combinedKey = ByteArray(24)
            for (i in combinedKey.indices) {
                combinedKey[i] = Random.nextInt(48, 58 + 26).toByte()
                if(combinedKey[i] > 57) {
                    combinedKey[i] = (combinedKey[i] + 7).toByte()
                }
            }
            return combinedKey.toString(StandardCharsets.UTF_8)
        }

        fun generateHMacKey(aesKey: String, hmacKey: String = generateRandomPlainTextHmacKey()): String {
            val hmacKeyData = hmacKey.toByteArray(StandardCharsets.UTF_8)
            val encryptedHmacKey = encryptAesCbcPkcs5Padding(aesKey, hmacKeyData)
            return Base64.getEncoder().encodeToString(encryptedHmacKey)
        }

        private fun generateRandomPlainTextHmacKey(): String {
            val key = ByteArray(4)
            for (i in key.indices) {
                key[i] = Random.nextInt(33, 126).toByte()
            }
            return key.toString(StandardCharsets.UTF_8)
        }

        private fun encryptAesCbcPkcs5Padding(key: String, data: ByteArray): ByteArray {
            val keyBytes = key.toByteArray(StandardCharsets.UTF_8)
            val rightSizedKey = keyBytes.copyOf(32)
            val ivBytes = keyBytes.copyOfRange(key.length - 16, key.length)
            val secretKeySpec = SecretKeySpec(rightSizedKey, "AES")
            val ivParameterSpec = IvParameterSpec(ivBytes)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
            return cipher.doFinal(data)
        }
    }
}
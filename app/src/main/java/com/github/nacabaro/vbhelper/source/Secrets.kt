package com.github.nacabaro.vbhelper.source

import com.github.cfogrady.vbnfc.CryptographicTransformer

data class Secrets(val hmacKey1: String, val hmacKey2: String, val aesKey: String, val substitutionCipher: IntArray) {
    fun toCryptographicTransformer(): CryptographicTransformer {
        return CryptographicTransformer(hmacKey1, hmacKey2, aesKey, substitutionCipher)
    }
}

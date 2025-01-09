package com.github.nacabaro.vbhelper.source

import com.github.cfogrady.vbnfc.data.DeviceType
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets


class DexFileSecretsImporter {
    companion object {

        const val VBDM_SUBSTITUTION_CIPHER_IDX = 1080145
        const val BE_SUBSTITUTION_CIPHER_IDX = 1080217

        const val VBDM_HMAC_KEY_2_IDX = 1249063
        const val VBDM_HMAC_KEY_1_IDX = 1494074
        const val VBC_HMAC_KEY_1_IDX = 1241640
        const val VBC_HMAC_KEY_2_IDX = 1466955
        const val BE_HMAC_KEY_1_IDX = 1580157
        const val BE_HMAC_KEY_2_IDX = 1593759
        const val AES_KEY_IDX = 1277527
    }

    fun importSecrets(inputStream: InputStream): Map<UShort, Secrets> {
        val dexFile = inputStream.readBytes()
        val byteOrder = ByteOrder.BIG_ENDIAN
        val vbdmSubstitutionCipher = dexFile.sliceArray(VBDM_SUBSTITUTION_CIPHER_IDX until VBDM_SUBSTITUTION_CIPHER_IDX+(16*4)).toIntArray(byteOrder)
        val beSubstitutionCipher = dexFile.sliceArray(BE_SUBSTITUTION_CIPHER_IDX until BE_SUBSTITUTION_CIPHER_IDX+(16*4)).toIntArray(byteOrder)
        val aesKey = dexFile.sliceArray(AES_KEY_IDX until AES_KEY_IDX+24).toString(StandardCharsets.UTF_8)
        val cryptographicTransformerByDevices = mapOf(
            Pair(DeviceType.VitalSeriesDeviceType, buildSecrets(dexFile, VBDM_HMAC_KEY_1_IDX, VBDM_HMAC_KEY_2_IDX, aesKey, vbdmSubstitutionCipher)),
            Pair(DeviceType.VitalBraceletBEDeviceType, buildSecrets(dexFile, BE_HMAC_KEY_1_IDX, BE_HMAC_KEY_2_IDX, aesKey, beSubstitutionCipher)),
            Pair(DeviceType.VitalCharactersDeviceType, buildSecrets(dexFile, VBC_HMAC_KEY_1_IDX, VBC_HMAC_KEY_2_IDX, aesKey, vbdmSubstitutionCipher)),
        )
        return cryptographicTransformerByDevices
    }

    private fun buildSecrets(dexFile: ByteArray, hmacKeyIdx1: Int, hmacKeyIdx2: Int, aesKey: String, substitutionCipher: IntArray): Secrets {
        val hmacKey1 = dexFile.sliceArray(hmacKeyIdx1 until hmacKeyIdx1+24).toString(StandardCharsets.UTF_8)
        val hmacKey2 = dexFile.sliceArray(hmacKeyIdx2 until hmacKeyIdx2+24).toString(StandardCharsets.UTF_8)
        return Secrets(hmacKey1, hmacKey2, aesKey, substitutionCipher)
    }
}

fun ByteArray.toIntArray(byteOrder: ByteOrder): IntArray {
    require(this.size % 4 == 0) { "Number of bytes must be multiple of 4 to convert into 32-bit words" }
    val values = IntArray(this.size / 4)
    ByteBuffer.wrap(this).order(byteOrder).asIntBuffer()[values]
    return values
}
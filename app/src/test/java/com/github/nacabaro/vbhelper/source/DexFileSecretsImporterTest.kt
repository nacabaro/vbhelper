package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.source.proto.Secrets.HmacKeys
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URL
import java.nio.ByteOrder
import java.security.InvalidKeyException


fun assertHmacKeysArePopulated(msg: String, hmacKeys: HmacKeys) {
    Assert.assertFalse("$msg hmacKey1 is empty", hmacKeys.hmacKey1.isEmpty())
    Assert.assertFalse("$msg hmacKey2 is empty", hmacKeys.hmacKey2.isEmpty())
}

class DexFileSecretsImporterTest {

    @Test
    fun testThatImportSecretsIsPopulated() {
        val dexFileSecretsImporter = DexFileSecretsImporter()
        val url = getAndAssertClassesDexFile()
        val file = File(url.path)
        file.inputStream().use {
            val secrets = dexFileSecretsImporter.importSecrets(it)
            Assert.assertEquals("Cipher size isn't correct", 16, secrets.vbCipherCount)
            Assert.assertEquals("BE Cipher size isn't correct", 16, secrets.beCipherCount)
            Assert.assertFalse("AES Key is empty", secrets.aesKey.isEmpty())
            assertHmacKeysArePopulated("VBDM", secrets.vbdmHmacKeys)
            assertHmacKeysArePopulated("VBC", secrets.vbcHmacKeys)
            assertHmacKeysArePopulated("BE", secrets.beHmacKeys)
        }
    }

    @Test
    fun testThatImportWrongSecretsThrows() {
        val dexFileSecretsImporter = DexFileSecretsImporter()
        val url = getAndAssertClassesDexFile()
        val file = File(url.path)
        val content = file.readBytes()
        val badCipher = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15).toByteArray(ByteOrder.BIG_ENDIAN)
        badCipher.copyInto(content, DexFileSecretsImporter.BE_SUBSTITUTION_CIPHER_IDX)
        ByteArrayInputStream(content).use {
            Assert.assertThrows("Secrets are validated", InvalidKeyException::class.java) {
                dexFileSecretsImporter.importSecrets(it)
            }
        }
    }

    private fun getAndAssertClassesDexFile(): URL {
        val url = javaClass.getResource("classes.dex")
        if(url == null) {
            Assert.assertTrue("""
                Create `resources\com\github\nacabaro\vbhelper\source` within the src/test directory.
                Add classes.dex from the official apk in the above directory. It should never be
                checked in and should be on the .gitignore.
            """.trimIndent(), false)
        }
        return url!!
    }
}

fun IntArray.toByteArray(byteOrder: ByteOrder = ByteOrder.nativeOrder()): ByteArray {
    val byteArray = ByteArray(this.size*4)
    for(i in this.indices) {
        val byteArrayIndex = i*4
        this[i].toByteArray(byteArray, byteArrayIndex, byteOrder)
    }
    return byteArray
}

fun Int.toByteArray(bytes: ByteArray, dstIndex: Int, byteOrder: ByteOrder = ByteOrder.nativeOrder()) {
    val asUInt = this.toUInt()
    for(i in 0 until 4) {
        if(byteOrder == ByteOrder.LITTLE_ENDIAN) {
            bytes[i+dstIndex] = ((asUInt shr 8*i) and 255u).toByte()
        } else {
            bytes[(3-i) + dstIndex] = ((asUInt shr 8*i) and 255u).toByte()
        }
    }
}
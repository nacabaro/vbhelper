package com.github.nacabaro.vbhelper.source

import com.github.cfogrady.vbnfc.data.DeviceType
import org.junit.Assert
import org.junit.Test
import java.io.File

class ApkSecretsImporterTest {

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun importSecretsTest() {
        val apkSecretsImporter = ApkSecretsImporter()
        val url = javaClass.getResource("com.bandai.vitalbraceletarena.apk")
        if(url == null) {
            Assert.assertTrue("""
                Create `resources\com\github\nacabaro\vbhelper\source` within the src/test directory.
                Add com.bandai.vitalbraceletarena.apk (the official apk) in the above directory. It
                should never be checked in and should be on the .gitignore.
            """.trimIndent(), false)
        }
        val file = File(url.path)
        val testTagId = byteArrayOf(0x04, 0x40, 0xaf.toByte(), 0xa2.toByte(), 0xee.toByte(), 0x0f, 0x90.toByte())
        file.inputStream().use {
            val deviceIdToCryptographicTransformer = apkSecretsImporter.importSecrets(it)
            var password = deviceIdToCryptographicTransformer[DeviceType.VitalBraceletBEDeviceType]!!.toCryptographicTransformer().createNfcPassword(testTagId)
            Assert.assertEquals("5651b1c8", password.toHexString())
            password = deviceIdToCryptographicTransformer[DeviceType.VitalSeriesDeviceType]!!.toCryptographicTransformer().createNfcPassword(testTagId)
            Assert.assertEquals("dd2ceb84", password.toHexString())
            password = deviceIdToCryptographicTransformer[DeviceType.VitalCharactersDeviceType]!!.toCryptographicTransformer().createNfcPassword(testTagId)
            Assert.assertEquals("515e0c12", password.toHexString())
        }
    }
}
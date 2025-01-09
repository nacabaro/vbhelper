package com.github.nacabaro.vbhelper.source

import com.github.cfogrady.vbnfc.data.DeviceType
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ApkSecretsImporterTest {

    @Test
    fun testThatRealImportSecretsHasAllDeviceTypes() {
        val apkFileSecretsImporter = ApkSecretsImporter()
        val url = getAndAssertApkFile()
        val file = File(url.path)
        file.inputStream().use {
            val deviceIdToSecrets = apkFileSecretsImporter.importSecrets(it)
            Assert.assertNotNull("BE Device Type", deviceIdToSecrets[DeviceType.VitalBraceletBEDeviceType])
            Assert.assertNotNull("VBDM Device Type", deviceIdToSecrets[DeviceType.VitalSeriesDeviceType])
            Assert.assertNotNull("VBC Device Type", deviceIdToSecrets[DeviceType.VitalCharactersDeviceType])
        }
    }

    @Test
    fun testThatApkSecretsImporterCallsDexSecretImporterOnDexFile() {
        val expectedDexContents = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 6, 5, 4, 3, 2, 1)
        var foundFile = false
        val apkFileSecretsImporter = ApkSecretsImporter {
            val inputStreamContents = it.readAllBytes()
            Assert.assertTrue("Unexpected file contents received by DexSecretsImporter", inputStreamContents.contentEquals(expectedDexContents))
            foundFile = true
            emptyMap()
        }
        val apkBytes = constructTestApk(expectedDexContents)
        ByteArrayInputStream(apkBytes).use {
            apkFileSecretsImporter.importSecrets(it)
        }
        Assert.assertTrue("${ApkSecretsImporter.DEX_FILE} not found", foundFile)
    }

    fun constructTestApk(dexContents: ByteArray): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ZipOutputStream(byteArrayOutputStream).use { zipOutputStream ->
            zipOutputStream.putNextEntry(ZipEntry("dummy.txt"))
            zipOutputStream.write("This is a text file".toByteArray(StandardCharsets.UTF_8))
            zipOutputStream.putNextEntry(ZipEntry("AndroidManifest.xml"))
            zipOutputStream.write("Malformed xml!".toByteArray(StandardCharsets.UTF_8))
            zipOutputStream.putNextEntry(ZipEntry("assets/"))
            zipOutputStream.putNextEntry(ZipEntry("assets/bad.assets"))
            zipOutputStream.write("Malformed asset!".toByteArray(StandardCharsets.UTF_8))
            zipOutputStream.putNextEntry(ZipEntry(ApkSecretsImporter.DEX_FILE))
            zipOutputStream.write(dexContents)
        }
        return byteArrayOutputStream.toByteArray()
    }

    private fun getAndAssertApkFile(): URL {
        val url = javaClass.getResource("com.bandai.vitalbraceletarena.apk")
        if(url == null) {
            Assert.assertTrue("""
                Create `resources\com\github\nacabaro\vbhelper\source` within the src/test directory.
                Add com.bandai.vitalbraceletarena.apk (the official apk) in the above directory. It
                should never be checked in and should be on the .gitignore.
            """.trimIndent(), false)
        }
        return url!!
    }
}
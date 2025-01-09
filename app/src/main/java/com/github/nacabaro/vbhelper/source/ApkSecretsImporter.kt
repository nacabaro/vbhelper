package com.github.nacabaro.vbhelper.source

import java.io.InputStream
import java.util.zip.ZipInputStream

class ApkSecretsImporter(private val dexFileSecretsImporter: DexFileSecretsImporter = DexFileSecretsImporter()) {

    companion object {
        const val DEX_FILE = "classes.dex"
    }

    fun importSecrets(inputStream: InputStream): Map<UShort, Secrets> {
        ZipInputStream(inputStream).use { zip ->
            var zipEntry = zip.nextEntry
            while(zipEntry != null) {
                println("Zip Entry: ${zipEntry.name}")
                if(zipEntry.name == DEX_FILE) {
                    return dexFileSecretsImporter.importSecrets(zip)
                }
                zipEntry = zip.nextEntry
            }
            throw IllegalArgumentException("File `$DEX_FILE` is missing from apk!")
        }
    }
}

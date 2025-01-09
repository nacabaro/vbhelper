package com.github.nacabaro.vbhelper.source

import java.io.InputStream
import java.util.zip.ZipInputStream

class ApkSecretsImporter(private val dexFileSecretsImporter: SecretsImporter = DexFileSecretsImporter()): SecretsImporter {

    companion object {
        const val DEX_FILE = "classes.dex"
    }

    // importSecrets imports the secrets from the apk input stream, and validates them.
    override fun importSecrets(inputStream: InputStream): Map<UShort, Secrets> {
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

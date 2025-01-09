package com.github.nacabaro.vbhelper.source

import java.io.InputStream

fun interface SecretsImporter {
    fun importSecrets(inputStream: InputStream): Map<UShort, Secrets>
}
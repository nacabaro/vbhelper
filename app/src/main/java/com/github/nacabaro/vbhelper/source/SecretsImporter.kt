package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.source.proto.Secrets
import java.io.InputStream

fun interface SecretsImporter {
    fun importSecrets(inputStream: InputStream): Secrets
}
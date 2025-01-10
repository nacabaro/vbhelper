package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.source.proto.Secrets
import kotlinx.coroutines.flow.Flow

interface SecretsRepository {
    val secretsFlow: Flow<Secrets>

    suspend fun getSecrets(): Secrets
    suspend fun updateSecrets(secrets: Secrets)
}
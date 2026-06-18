package com.github.nacabaro.vbhelper.di

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.source.CurrencyRepository
import com.github.nacabaro.vbhelper.source.DataStoreSecretsRepository
import com.github.nacabaro.vbhelper.companion.validation.ValidatedCardManager
import com.github.nacabaro.vbhelper.companion.logs.CompanionLogService

interface AppContainer {
    val db: AppDatabase
    val dataStoreSecretsRepository: DataStoreSecretsRepository
    val currencyRepository: CurrencyRepository
    val validatedCardManager: ValidatedCardManager
    val companionLogService: CompanionLogService
}
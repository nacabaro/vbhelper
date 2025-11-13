package com.github.nacabaro.vbhelper.di

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.source.CurrencyRepository
import com.github.nacabaro.vbhelper.source.DataStoreSecretsRepository

interface AppContainer {
    val db: AppDatabase
    val dataStoreSecretsRepository: DataStoreSecretsRepository
    val currencyRepository: CurrencyRepository
}
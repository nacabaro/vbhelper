package com.github.nacabaro.vbhelper.di

import com.github.cfogrady.vitalwear.common.data.SharedTransferSeenDao
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.source.CurrencyRepository
import com.github.nacabaro.vbhelper.source.DataStoreSecretsRepository

interface AppContainer {
    val db: AppDatabase
    val transferSeenDao: SharedTransferSeenDao
    val dataStoreSecretsRepository: DataStoreSecretsRepository
    val currencyRepository: CurrencyRepository
}
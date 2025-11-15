package com.github.nacabaro.vbhelper.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CurrencyRepository (
    private val dataStore: DataStore<Preferences>
) {

    private companion object {
        val CURRENCY_VALUE = intPreferencesKey("currency_value")
    }

    val currencyValue: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[CURRENCY_VALUE] ?: 10000
        }

    suspend fun setCurrencyValue(newValue: Int) {
        dataStore.edit { preferences ->
            preferences[CURRENCY_VALUE] = newValue
        }
    }
}
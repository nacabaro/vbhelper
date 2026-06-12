package com.github.nacabaro.vbhelper.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CardSettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val ENABLE_DIM_TO_BEM_CONVERSION = booleanPreferencesKey("enable_dim_to_bem_conversion")
    }

    val enableDimToBemConversion: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[ENABLE_DIM_TO_BEM_CONVERSION] ?: false
        }

    suspend fun setEnableDimToBemConversion(enable: Boolean) {
        dataStore.edit { preferences ->
            preferences[ENABLE_DIM_TO_BEM_CONVERSION] = enable
        }
    }
}


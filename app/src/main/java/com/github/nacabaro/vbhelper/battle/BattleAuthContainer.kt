package com.github.nacabaro.vbhelper.battle

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.github.nacabaro.vbhelper.source.AuthRepository

private const val BATTLE_AUTH_PREFERENCES_NAME = "battle_auth_preferences"
val Context.battleAuthStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(
    name = BATTLE_AUTH_PREFERENCES_NAME
)

class BattleAuthContainer(private val context: Context) {
    val authRepository: AuthRepository = AuthRepository(context.battleAuthStore)
}


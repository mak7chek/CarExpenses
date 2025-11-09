package com.mak7chek.carexpenses.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeSetting {
    SYSTEM, LIGHT, DARK
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val THEME_SETTING_KEY = stringPreferencesKey("theme_setting")

    val themeSetting: Flow<ThemeSetting> = context.dataStore.data
        .map { preferences ->
            ThemeSetting.valueOf(
                preferences[THEME_SETTING_KEY] ?: ThemeSetting.SYSTEM.name
            )
        }

    suspend fun saveThemeSetting(theme: ThemeSetting) {
        context.dataStore.edit { settings ->
            settings[THEME_SETTING_KEY] = theme.name
        }
    }
}
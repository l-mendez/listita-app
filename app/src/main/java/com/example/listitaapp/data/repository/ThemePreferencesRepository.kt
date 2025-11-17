package com.example.listitaapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.listitaapp.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemePreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val themeKey = stringPreferencesKey("theme_preference")

    val themePreference: Flow<ThemePreference> =
        dataStore.data.map { prefs -> ThemePreference.fromValue(prefs[themeKey]) }

    suspend fun setThemePreference(preference: ThemePreference) {
        dataStore.edit { prefs -> prefs[themeKey] = preference.value }
    }
}

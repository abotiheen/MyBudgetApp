package com.example.mybudgetapp.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    System,
    Light,
    Dark,
}

interface ThemePreferenceRepository {
    val themeMode: StateFlow<AppThemeMode>
    fun setThemeMode(mode: AppThemeMode)
}

class SharedPreferencesThemePreferenceRepository(
    context: Context,
) : ThemePreferenceRepository {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(loadThemeMode())

    override val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    override fun setThemeMode(mode: AppThemeMode) {
        preferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    private fun loadThemeMode(): AppThemeMode {
        val storedValue = preferences.getString(KEY_THEME_MODE, null)
        return storedValue?.let {
            runCatching { AppThemeMode.valueOf(it) }.getOrNull()
        } ?: AppThemeMode.System
    }

    companion object {
        private const val PREFERENCES_NAME = "theme_preferences"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}

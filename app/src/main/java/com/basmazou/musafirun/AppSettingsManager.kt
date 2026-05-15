package com.basmazou.musafirun

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat


object AppSettingsManager {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_DARK_MODE = "dark_mode_enabled"
    private const val KEY_LANGUAGE = "app_language"
    private const val DEFAULT_LANGUAGE = "ca"

    fun applySavedSettings(context: Context) {
        applyTheme(isDarkModeEnabled(context))
        applyLanguage(getCurrentLanguage(context))
    }

    fun isDarkModeEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkModeEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .apply()
        applyTheme(enabled)
    }

    fun getCurrentLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, DEFAULT_LANGUAGE)
            .orEmpty()
            .ifBlank { DEFAULT_LANGUAGE }
    }

    fun setCurrentLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, languageCode)
            .apply()
        applyLanguage(languageCode)
    }

    private fun applyTheme(darkModeEnabled: Boolean) {
        val desiredMode = if (darkModeEnabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        if (AppCompatDelegate.getDefaultNightMode() != desiredMode) {
            AppCompatDelegate.setDefaultNightMode(desiredMode)
        }
    }

    private fun applyLanguage(languageCode: String) {
        val locales = LocaleListCompat.forLanguageTags(languageCode)
        if (AppCompatDelegate.getApplicationLocales() != locales) {
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }
}



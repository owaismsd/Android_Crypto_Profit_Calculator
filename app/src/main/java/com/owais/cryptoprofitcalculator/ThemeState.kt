package com.owais.cryptoprofitcalculator

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ThemeState {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_IS_DARK = "is_dark_theme"

    private var prefs: SharedPreferences? = null

    // null = follow system default
    var isDarkTheme by mutableStateOf<Boolean?>(null)

    // Load the saved theme when the app starts
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // We use -1 for "not set yet", 1 for Dark, 0 for Light
        val savedTheme = prefs?.getInt(KEY_IS_DARK, -1) ?: -1
        isDarkTheme = when (savedTheme) {
            1 -> true
            0 -> false
            else -> null
        }
    }

    // Save the new theme when the user toggles the switch
    fun saveTheme(isDark: Boolean) {
        isDarkTheme = isDark
        prefs?.edit()?.putInt(KEY_IS_DARK, if (isDark) 1 else 0)?.apply()
    }
}
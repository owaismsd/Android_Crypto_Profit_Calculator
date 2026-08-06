package com.owais.cryptoprofitcalculator

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AvatarPrefs {
    private const val PREFS_NAME = "CryptoAvatarPrefs"
    private const val KEY_AVATAR = "selected_avatar_id"

    // Default avatar ID
    var currentAvatarId by mutableStateOf("crypto_bull")
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentAvatarId = prefs.getString(KEY_AVATAR, "crypto_bull") ?: "crypto_bull"
    }

    fun saveAvatar(context: Context, avatarId: String) {
        currentAvatarId = avatarId
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_AVATAR, avatarId).apply()
    }
}
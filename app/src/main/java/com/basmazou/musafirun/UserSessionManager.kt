package com.basmazou.musafirun

import android.content.Context
import java.util.UUID

object UserSessionManager {
    private const val PREFS_NAME = "loginPrefs"
    private const val KEY_USER = "usuari"
    private const val KEY_GUEST_ID = "guest_id"
    private const val KEY_GUEST_MODE = "guest_mode"

    fun getCurrentUserId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (prefs.getBoolean(KEY_GUEST_MODE, false)) {
            val guestId = prefs.getString(KEY_GUEST_ID, null)
            if (!guestId.isNullOrBlank()) return guestId
            val newGuestId = "guest_${UUID.randomUUID()}"
            prefs.edit().putString(KEY_GUEST_ID, newGuestId).apply()
            return newGuestId
        }

        val usuariGuardat = prefs.getString(KEY_USER, null)
        if (!usuariGuardat.isNullOrBlank()) {
            return usuariGuardat
        }

        val convidatId = prefs.getString(KEY_GUEST_ID, null)
        if (!convidatId.isNullOrBlank()) {
            return convidatId
        }

        val nouConvidatId = "guest_${UUID.randomUUID()}"
        prefs.edit().putString(KEY_GUEST_ID, nouConvidatId).apply()
        return nouConvidatId
    }

    fun startGuestSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove(KEY_USER)
        editor.putBoolean("recorda", false)
        editor.putBoolean(KEY_GUEST_MODE, true)
        if (prefs.getString(KEY_GUEST_ID, null).isNullOrBlank()) {
            editor.putString(KEY_GUEST_ID, "guest_${UUID.randomUUID()}")
        }
        editor.apply()
    }

    fun startRegisteredSession(context: Context, userId: String, remember: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_USER, userId)
            .putBoolean("recorda", remember)
            .putBoolean(KEY_GUEST_MODE, false)
            .apply()
    }
}



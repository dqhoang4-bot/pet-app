package com.pethealth.passport.data.repository

import android.content.Context
import androidx.core.content.edit

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("pet_health_settings", Context.MODE_PRIVATE)

    var language: String
        get() = prefs.getString("language", "vi") ?: "vi"
        set(value) = prefs.edit { putString("language", value) }

    var ownerName: String
        get() = prefs.getString("owner_name", "Người nuôi thú cưng") ?: "Người nuôi thú cưng"
        set(value) = prefs.edit { putString("owner_name", value) }

    var reminderEnabled: Boolean
        get() = prefs.getBoolean("reminder_enabled", true)
        set(value) = prefs.edit { putBoolean("reminder_enabled", value) }

    var reminderHour: Int
        get() = prefs.getInt("reminder_hour", 9)
        set(value) = prefs.edit { putInt("reminder_hour", value) }

    var seeded: Boolean
        get() = prefs.getBoolean("seeded", false)
        set(value) = prefs.edit { putBoolean("seeded", value) }

    fun isEnglish(): Boolean = language == "en"
}

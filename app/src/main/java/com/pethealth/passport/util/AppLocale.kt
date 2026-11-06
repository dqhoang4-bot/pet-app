package com.pethealth.passport.util

import android.content.Context
import android.content.res.Configuration
import com.pethealth.passport.data.repository.SettingsRepository
import java.util.Locale

/**
 * Applies the language saved in settings to a context. The app defaults to
 * Vietnamese on first launch instead of following the device language.
 */
object AppLocale {

    const val VIETNAMESE = "vi"
    const val ENGLISH = "en"

    fun current(context: Context): String =
        SettingsRepository(context).language.takeIf { it == VIETNAMESE || it == ENGLISH }
            ?: VIETNAMESE

    fun wrap(context: Context): Context {
        val locale = Locale(current(context))
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        return context.createConfigurationContext(config)
    }
}

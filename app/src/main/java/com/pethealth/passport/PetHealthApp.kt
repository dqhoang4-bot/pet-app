package com.pethealth.passport

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.pethealth.passport.di.AppContainer
import com.pethealth.passport.util.AppLocale
import com.pethealth.passport.util.NotificationHelper
import com.pethealth.passport.util.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PetHealthApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        val savedLanguage = container.settings.language
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedLanguage))
        }

        NotificationHelper.ensureChannel(this)
        if (container.settings.reminderEnabled) {
            ReminderScheduler.schedule(this, container.settings.reminderHour)
        } else {
            ReminderScheduler.cancel(this)
        }

        appScope.launch {
            container.seeder.seedIfNeeded()
            container.communityRepository.refreshRemote()
        }
    }
}

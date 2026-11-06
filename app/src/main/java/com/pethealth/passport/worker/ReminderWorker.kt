package com.pethealth.passport.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pethealth.passport.data.local.AppDatabase
import com.pethealth.passport.data.repository.SettingsRepository
import com.pethealth.passport.util.Dates
import com.pethealth.passport.util.NotificationHelper

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = SettingsRepository(applicationContext)
        if (!settings.reminderEnabled) return Result.success()
        if (!NotificationHelper.canNotify(applicationContext)) return Result.success()

        val dao = AppDatabase.get(applicationContext).healthDao()
        val until = Dates.addDays(System.currentTimeMillis(), 7)
        val upcoming = dao.getUpcoming(until)
        if (upcoming.isEmpty()) return Result.success()

        upcoming.groupBy { it.vaccination.petId }.forEach { (petId, items) ->
            val petName = items.first().petName
            val lines = items.take(4).map { item ->
                val v = item.vaccination
                val days = Dates.daysFromToday(v.nextDueDate ?: 0L)
                val due = when {
                    days < 0 -> applicationContext.getString(
                        com.pethealth.passport.R.string.reminder_overdue,
                        Math.abs(days)
                    )
                    days == 0L -> applicationContext.getString(
                        com.pethealth.passport.R.string.reminder_today
                    )
                    else -> applicationContext.getString(
                        com.pethealth.passport.R.string.reminder_in_days,
                        days
                    )
                }
                "${v.name} – $due"
            }
            NotificationHelper.showPetReminder(applicationContext, petId, petName, lines)
        }
        return Result.success()
    }
}

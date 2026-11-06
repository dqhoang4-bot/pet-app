package com.pethealth.passport.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Dates {
    const val DAY_MS = 24L * 60L * 60L * 1000L

    fun format(millis: Long, pattern: String = "dd/MM/yyyy"): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))

    fun formatMonthYear(millis: Long): String =
        format(millis, "MM/yyyy")

    fun parse(text: String, pattern: String = "dd/MM/yyyy"): Long? = runCatching {
        SimpleDateFormat(pattern, Locale.getDefault()).parse(text)?.time
    }.getOrNull()

    fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun daysFromToday(millis: Long): Long {
        val today = startOfDay(System.currentTimeMillis())
        val target = startOfDay(millis)
        return Math.round((target - today).toDouble() / DAY_MS)
    }

    fun addDays(millis: Long, days: Int): Long = millis + days * DAY_MS

    fun addMonths(millis: Long, months: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            add(Calendar.MONTH, months)
        }
        return cal.timeInMillis
    }

    fun nextDose(date: Long, kind: String): Long =
        addMonths(date, if (kind == "dewormer") 3 else 12)

    fun ageParts(birthDate: Long): Triple<Int, Int, Int>? {
        if (birthDate <= 0L) return null
        val birth = Calendar.getInstance().apply { timeInMillis = birthDate }
        val now = Calendar.getInstance()
        var years = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        var months = now.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
        if (now.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH)) months--
        if (months < 0) {
            years--
            months += 12
        }
        return Triple(years.coerceAtLeast(0), months.coerceAtLeast(0), 0)
    }
}

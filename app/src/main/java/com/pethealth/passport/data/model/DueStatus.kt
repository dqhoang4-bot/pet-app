package com.pethealth.passport.data.model

import com.pethealth.passport.data.local.entity.Vaccination
import com.pethealth.passport.util.Dates

enum class DueStatus {
    OVERDUE,
    DUE_SOON,
    UPCOMING,
    NONE
}

fun Vaccination.dueStatus(now: Long = System.currentTimeMillis()): DueStatus {
    val due = nextDueDate ?: return DueStatus.NONE
    val days = Dates.daysFromToday(due)
    return when {
        days < 0 -> DueStatus.OVERDUE
        days <= 7 -> DueStatus.DUE_SOON
        else -> DueStatus.UPCOMING
    }
}

fun Vaccination.isDueWithin(days: Int, now: Long = System.currentTimeMillis()): Boolean {
    val due = nextDueDate ?: return false
    return Dates.daysFromToday(due) <= days
}

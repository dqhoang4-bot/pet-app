package com.pethealth.passport.ui.common

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.pethealth.passport.R
import com.pethealth.passport.data.local.entity.Pet
import com.pethealth.passport.data.model.DueStatus
import com.pethealth.passport.data.model.Species
import com.pethealth.passport.data.repository.ArticleCategory
import com.pethealth.passport.util.Dates
import java.util.TimeZone

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(message: String) {
    requireContext().toast(message)
}

fun Fragment.pickDate(initialMillis: Long?, onPick: (Long) -> Unit) {
    val picker = MaterialDatePicker.Builder.datePicker()
        .setTitleText(R.string.select_date)
        .setSelection(initialMillis ?: System.currentTimeMillis())
        .build()
    picker.addOnPositiveButtonClickListener { utcMillis ->
        val local = utcMillis + TimeZone.getDefault().getOffset(utcMillis)
        onPick(local)
    }
    picker.show(childFragmentManager, "date_picker")
}

fun Pet.emoji(): String = Species.emoji(species)

fun Pet.speciesLabel(context: Context): String =
    context.getString(Species.option(species).labelRes)

fun Pet.ageLabel(context: Context): String {
    val (years, months, _) = Dates.ageParts(birthDate) ?: return context.getString(R.string.age_unknown)
    return when {
        years == 0 && months == 0 -> context.getString(R.string.age_newborn)
        years == 0 -> context.getString(R.string.age_months, months)
        months == 0 -> context.getString(R.string.age_years, years)
        else -> context.getString(R.string.age_years_months, years, months)
    }
}

fun DueStatus.label(context: Context): String = when (this) {
    DueStatus.OVERDUE -> context.getString(R.string.status_overdue)
    DueStatus.DUE_SOON -> context.getString(R.string.status_due_soon)
    DueStatus.UPCOMING -> context.getString(R.string.status_upcoming)
    DueStatus.NONE -> context.getString(R.string.status_none)
}

fun DueStatus.color(context: Context): Int = when (this) {
    DueStatus.OVERDUE -> context.getColor(R.color.status_overdue)
    DueStatus.DUE_SOON -> context.getColor(R.color.status_due_soon)
    DueStatus.UPCOMING -> context.getColor(R.color.status_upcoming)
    DueStatus.NONE -> context.getColor(R.color.text_secondary)
}

fun ArticleCategory.label(context: Context): String = when (this) {
    ArticleCategory.NUTRITION -> context.getString(R.string.category_nutrition)
    ArticleCategory.DISEASE -> context.getString(R.string.category_disease)
    ArticleCategory.CARE -> context.getString(R.string.category_care)
}

fun dueText(context: Context, dueDate: Long?): String {
    if (dueDate == null) return context.getString(R.string.no_due_date)
    val days = Dates.daysFromToday(dueDate)
    return when {
        days < 0 -> context.getString(R.string.due_overdue_days, Math.abs(days))
        days == 0L -> context.getString(R.string.due_today)
        days == 1L -> context.getString(R.string.due_tomorrow)
        else -> context.getString(R.string.due_in_days, days)
    }
}

fun relativeTime(context: Context, millis: Long): String {
    val diff = (System.currentTimeMillis() - millis).coerceAtLeast(0L)
    val minutes = diff / 60_000L
    val hours = diff / 3_600_000L
    val days = diff / 86_400_000L
    return when {
        minutes < 1 -> context.getString(R.string.time_just_now)
        minutes < 60 -> context.getString(R.string.time_minutes_ago, minutes)
        hours < 24 -> context.getString(R.string.time_hours_ago, hours)
        else -> context.getString(R.string.time_days_ago, days)
    }
}

package com.pethealth.passport.ui.common

import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import coil.load
import com.pethealth.passport.R
import com.pethealth.passport.data.local.entity.Pet
import com.pethealth.passport.data.model.DueStatus
import com.pethealth.passport.data.model.dueStatus
import com.pethealth.passport.data.model.Species
import com.pethealth.passport.data.local.entity.Vaccination
import java.io.File

fun bindPetAvatar(photo: ImageView, emoji: TextView, pet: Pet) {
    val path = pet.photoUri
    emoji.text = Species.emoji(pet.species)
    if (path.isNullOrBlank()) {
        photo.visible(false)
        emoji.visible(true)
    } else {
        photo.visible(true)
        emoji.visible(false)
        photo.load(File(path.removePrefix("file://"))) {
            crossfade(true)
            placeholder(R.drawable.bg_avatar)
            error(R.drawable.bg_avatar)
        }
    }
}

fun bindStatusPill(text: TextView, vaccination: Vaccination) {
    val status = vaccination.dueStatus()
    val context = text.context
    text.text = status.label(context)
    val bg = when (status) {
        DueStatus.OVERDUE -> R.color.status_overdue_bg
        DueStatus.DUE_SOON -> R.color.status_due_soon_bg
        DueStatus.UPCOMING -> R.color.status_upcoming_bg
        DueStatus.NONE -> R.color.surface_variant
    }
    val fg = when (status) {
        DueStatus.OVERDUE -> R.color.status_overdue
        DueStatus.DUE_SOON -> R.color.status_due_soon
        DueStatus.UPCOMING -> R.color.status_upcoming
        DueStatus.NONE -> R.color.text_secondary
    }
    text.backgroundTintList = ContextCompat.getColorStateList(context, bg)
    text.setTextColor(ContextCompat.getColor(context, fg))
}

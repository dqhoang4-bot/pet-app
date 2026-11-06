package com.pethealth.passport.data.local.model

import androidx.room.Embedded
import com.pethealth.passport.data.local.entity.Vaccination

data class UpcomingVaccination(
    @Embedded val vaccination: Vaccination,
    val petName: String,
    val petPhotoUri: String?
)

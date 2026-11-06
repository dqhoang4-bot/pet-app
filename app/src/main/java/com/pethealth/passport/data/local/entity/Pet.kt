package com.pethealth.passport.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val species: String,
    val breed: String = "",
    val gender: String = "unknown",
    val birthDate: Long = 0L,
    val weightKg: Double = 0.0,
    val color: String = "",
    val microchip: String = "",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val photoUri: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

package com.pethealth.passport.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_entries",
    foreignKeys = [
        ForeignKey(
            entity = Pet::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("petId")]
)
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val petId: Long,
    val date: Long,
    val weightKg: Double,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

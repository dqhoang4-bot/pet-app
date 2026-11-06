package com.pethealth.passport.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medical_records",
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
data class MedicalRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val petId: Long,
    val date: Long,
    val clinic: String = "",
    val vetName: String = "",
    val diagnosis: String = "",
    val prescription: String = "",
    val note: String = "",
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

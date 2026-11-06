package com.pethealth.passport.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vaccinations",
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
data class Vaccination(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val petId: Long,
    val name: String,
    val kind: String = KIND_VACCINE,
    val date: Long,
    val nextDueDate: Long? = null,
    val clinic: String = "",
    val vetName: String = "",
    val note: String = "",
    val reminderEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val KIND_VACCINE = "vaccine"
        const val KIND_DEWORMER = "dewormer"
    }
}

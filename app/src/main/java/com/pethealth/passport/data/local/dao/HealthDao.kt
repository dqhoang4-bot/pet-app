package com.pethealth.passport.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pethealth.passport.data.local.entity.MedicalRecord
import com.pethealth.passport.data.local.entity.Vaccination
import com.pethealth.passport.data.local.entity.WeightEntry
import com.pethealth.passport.data.local.model.UpcomingVaccination
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Query("SELECT * FROM vaccinations WHERE petId = :petId ORDER BY date DESC")
    fun observeVaccinations(petId: Long): Flow<List<Vaccination>>

    @Query("SELECT * FROM vaccinations WHERE petId = :petId ORDER BY date DESC")
    suspend fun getVaccinations(petId: Long): List<Vaccination>

    @Query("SELECT * FROM vaccinations ORDER BY date DESC")
    fun observeAllVaccinations(): Flow<List<Vaccination>>

    @Query("SELECT * FROM vaccinations WHERE id = :id")
    suspend fun getVaccination(id: Long): Vaccination?

    @Query(
        "SELECT v.*, p.name AS petName, p.photoUri AS petPhotoUri " +
            "FROM vaccinations v INNER JOIN pets p ON p.id = v.petId " +
            "WHERE v.nextDueDate IS NOT NULL AND v.nextDueDate <= :until AND v.reminderEnabled = 1 " +
            "ORDER BY v.nextDueDate ASC"
    )
    fun observeUpcoming(until: Long): Flow<List<UpcomingVaccination>>

    @Query(
        "SELECT v.*, p.name AS petName, p.photoUri AS petPhotoUri " +
            "FROM vaccinations v INNER JOIN pets p ON p.id = v.petId " +
            "WHERE v.nextDueDate IS NOT NULL AND v.nextDueDate <= :until AND v.reminderEnabled = 1 " +
            "ORDER BY v.nextDueDate ASC"
    )
    suspend fun getUpcoming(until: Long): List<UpcomingVaccination>

    @Insert
    suspend fun insertVaccination(vaccination: Vaccination): Long

    @Update
    suspend fun updateVaccination(vaccination: Vaccination)

    @Delete
    suspend fun deleteVaccination(vaccination: Vaccination)

    @Query("SELECT * FROM medical_records WHERE petId = :petId ORDER BY date DESC")
    fun observeMedicalRecords(petId: Long): Flow<List<MedicalRecord>>

    @Query("SELECT * FROM medical_records WHERE id = :id")
    suspend fun getMedicalRecord(id: Long): MedicalRecord?

    @Insert
    suspend fun insertMedicalRecord(record: MedicalRecord): Long

    @Update
    suspend fun updateMedicalRecord(record: MedicalRecord)

    @Delete
    suspend fun deleteMedicalRecord(record: MedicalRecord)

    @Query("SELECT * FROM weight_entries WHERE petId = :petId ORDER BY date ASC")
    fun observeWeights(petId: Long): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries WHERE id = :id")
    suspend fun getWeight(id: Long): WeightEntry?

    @Insert
    suspend fun insertWeight(entry: WeightEntry): Long

    @Update
    suspend fun updateWeight(entry: WeightEntry)

    @Delete
    suspend fun deleteWeight(entry: WeightEntry)

    @Query("DELETE FROM vaccinations")
    suspend fun deleteAllVaccinations()

    @Query("DELETE FROM medical_records")
    suspend fun deleteAllMedicalRecords()

    @Query("DELETE FROM weight_entries")
    suspend fun deleteAllWeights()
}

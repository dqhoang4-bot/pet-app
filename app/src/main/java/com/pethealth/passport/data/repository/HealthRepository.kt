package com.pethealth.passport.data.repository

import com.pethealth.passport.data.local.dao.HealthDao
import com.pethealth.passport.data.local.dao.PetDao
import com.pethealth.passport.data.local.entity.MedicalRecord
import com.pethealth.passport.data.local.entity.Vaccination
import com.pethealth.passport.data.local.entity.WeightEntry
import com.pethealth.passport.data.local.model.UpcomingVaccination
import com.pethealth.passport.util.Dates
import kotlinx.coroutines.flow.Flow

class HealthRepository(
    private val healthDao: HealthDao,
    private val petDao: PetDao
) {
    fun vaccinations(petId: Long): Flow<List<Vaccination>> =
        healthDao.observeVaccinations(petId)

    fun upcoming(days: Int = 30): Flow<List<UpcomingVaccination>> =
        healthDao.observeUpcoming(Dates.addDays(System.currentTimeMillis(), days))

    fun medicalRecords(petId: Long): Flow<List<MedicalRecord>> =
        healthDao.observeMedicalRecords(petId)

    fun weights(petId: Long): Flow<List<WeightEntry>> =
        healthDao.observeWeights(petId)

    suspend fun getVaccination(id: Long): Vaccination? = healthDao.getVaccination(id)

    suspend fun getMedicalRecord(id: Long): MedicalRecord? = healthDao.getMedicalRecord(id)

    suspend fun getWeight(id: Long): WeightEntry? = healthDao.getWeight(id)

    suspend fun saveVaccination(vaccination: Vaccination): Long {
        val withDue = if (vaccination.nextDueDate == null && vaccination.reminderEnabled) {
            vaccination.copy(nextDueDate = Dates.nextDose(vaccination.date, vaccination.kind))
        } else vaccination
        return if (withDue.id == 0L) {
            healthDao.insertVaccination(withDue)
        } else {
            healthDao.updateVaccination(withDue)
            withDue.id
        }
    }

    suspend fun deleteVaccination(vaccination: Vaccination) =
        healthDao.deleteVaccination(vaccination)

    suspend fun saveMedicalRecord(record: MedicalRecord): Long =
        if (record.id == 0L) healthDao.insertMedicalRecord(record)
        else {
            healthDao.updateMedicalRecord(record)
            record.id
        }

    suspend fun deleteMedicalRecord(record: MedicalRecord) =
        healthDao.deleteMedicalRecord(record)

    suspend fun saveWeight(entry: WeightEntry): Long {
        val id = if (entry.id == 0L) healthDao.insertWeight(entry)
        else {
            healthDao.updateWeight(entry)
            entry.id
        }
        petDao.getById(entry.petId)?.let { pet ->
            petDao.update(pet.copy(weightKg = entry.weightKg))
        }
        return id
    }

    suspend fun deleteWeight(entry: WeightEntry) {
        healthDao.deleteWeight(entry)
    }
}

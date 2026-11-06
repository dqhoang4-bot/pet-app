package com.pethealth.passport.ui.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealth.passport.data.local.entity.MedicalRecord
import com.pethealth.passport.data.local.entity.Pet
import com.pethealth.passport.data.local.entity.Vaccination
import com.pethealth.passport.data.local.entity.WeightEntry
import com.pethealth.passport.data.repository.HealthRepository
import com.pethealth.passport.data.repository.PetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PetDetailUiState(
    val pet: Pet? = null,
    val vaccinations: List<Vaccination> = emptyList(),
    val medical: List<MedicalRecord> = emptyList(),
    val weights: List<WeightEntry> = emptyList()
)

class PetDetailViewModel(
    petId: Long,
    petRepository: PetRepository,
    healthRepository: HealthRepository
) : ViewModel() {

    val state: StateFlow<PetDetailUiState> = combine(
        petRepository.observePet(petId),
        healthRepository.vaccinations(petId),
        healthRepository.medicalRecords(petId),
        healthRepository.weights(petId)
    ) { pet, vaccinations, medical, weights ->
        PetDetailUiState(
            pet = pet,
            vaccinations = vaccinations,
            medical = medical,
            weights = weights
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PetDetailUiState()
    )
}

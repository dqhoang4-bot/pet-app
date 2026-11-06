package com.pethealth.passport.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealth.passport.data.local.entity.Vaccination
import com.pethealth.passport.data.repository.HealthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class VaccinationListViewModel(
    petId: Long,
    healthRepository: HealthRepository
) : ViewModel() {

    val vaccinations: StateFlow<List<Vaccination>> = healthRepository.vaccinations(petId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
}

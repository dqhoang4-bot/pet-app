package com.pethealth.passport.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealth.passport.data.local.entity.Pet
import com.pethealth.passport.data.local.model.UpcomingVaccination
import com.pethealth.passport.data.repository.HealthRepository
import com.pethealth.passport.data.repository.PetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val pets: List<Pet> = emptyList(),
    val upcoming: List<UpcomingVaccination> = emptyList()
)

class HomeViewModel(
    petRepository: PetRepository,
    healthRepository: HealthRepository
) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(
        petRepository.pets,
        healthRepository.upcoming(30)
    ) { pets, upcoming ->
        HomeUiState(pets = pets, upcoming = upcoming)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState()
    )
}

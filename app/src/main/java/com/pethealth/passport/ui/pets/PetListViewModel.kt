package com.pethealth.passport.ui.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealth.passport.data.local.entity.Pet
import com.pethealth.passport.data.repository.PetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PetListViewModel(petRepository: PetRepository) : ViewModel() {
    val pets: StateFlow<List<Pet>> = petRepository.pets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
}

package com.pethealth.passport.ui.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealth.passport.data.local.entity.Pet
import com.pethealth.passport.data.repository.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PetEditViewModel(
    private val petRepository: PetRepository,
    private val petId: Long
) : ViewModel() {

    private val _pet = MutableStateFlow<Pet?>(null)
    val pet: StateFlow<Pet?> = _pet

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded

    init {
        viewModelScope.launch {
            if (petId > 0) _pet.value = petRepository.getPet(petId)
            _loaded.value = true
        }
    }

    suspend fun save(pet: Pet): Long = petRepository.save(pet)

    suspend fun delete(pet: Pet) = petRepository.delete(pet)
}

package com.pethealth.passport.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealth.passport.data.local.entity.Vaccination
import com.pethealth.passport.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VaccinationEditViewModel(
    private val petId: Long,
    private val vaccinationId: Long,
    private val healthRepository: HealthRepository
) : ViewModel() {

    private val _vaccination = MutableStateFlow<Vaccination?>(null)
    val vaccination: StateFlow<Vaccination?> = _vaccination

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded

    init {
        viewModelScope.launch {
            if (vaccinationId > 0) {
                _vaccination.value = healthRepository.getVaccination(vaccinationId)
            }
            _loaded.value = true
        }
    }

    suspend fun save(vaccination: Vaccination): Long = healthRepository.saveVaccination(vaccination)

    suspend fun delete(vaccination: Vaccination) = healthRepository.deleteVaccination(vaccination)
}

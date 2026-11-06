package com.pethealth.passport.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealth.passport.data.local.entity.WeightEntry
import com.pethealth.passport.data.repository.HealthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class WeightViewModel(
    petId: Long,
    private val healthRepository: HealthRepository
) : ViewModel() {

    val weights: StateFlow<List<WeightEntry>> = healthRepository.weights(petId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    suspend fun save(entry: WeightEntry): Long = healthRepository.saveWeight(entry)

    suspend fun delete(entry: WeightEntry) = healthRepository.deleteWeight(entry)
}

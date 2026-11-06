package com.pethealth.passport.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealth.passport.data.local.entity.MedicalRecord
import com.pethealth.passport.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MedicalEditViewModel(
    private val recordId: Long,
    private val healthRepository: HealthRepository
) : ViewModel() {

    private val _record = MutableStateFlow<MedicalRecord?>(null)
    val record: StateFlow<MedicalRecord?> = _record

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded

    init {
        viewModelScope.launch {
            if (recordId > 0) {
                _record.value = healthRepository.getMedicalRecord(recordId)
            }
            _loaded.value = true
        }
    }

    suspend fun save(record: MedicalRecord): Long = healthRepository.saveMedicalRecord(record)

    suspend fun delete(record: MedicalRecord) = healthRepository.deleteMedicalRecord(record)
}

package com.pethealth.passport.ui.settings

import androidx.lifecycle.ViewModel
import com.pethealth.passport.data.repository.SettingsRepository
import com.pethealth.passport.data.seed.DatabaseSeeder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val language: String,
    val ownerName: String,
    val reminderEnabled: Boolean,
    val reminderHour: Int
)

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val seeder: DatabaseSeeder
) : ViewModel() {

    private val _state = MutableStateFlow(read())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private fun read() = SettingsUiState(
        language = settings.language,
        ownerName = settings.ownerName,
        reminderEnabled = settings.reminderEnabled,
        reminderHour = settings.reminderHour
    )

    fun setLanguage(code: String) {
        settings.language = code
        _state.value = read()
    }

    fun setOwnerName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) settings.ownerName = trimmed
        _state.value = read()
    }

    fun setReminderEnabled(enabled: Boolean) {
        settings.reminderEnabled = enabled
        _state.value = read()
    }

    fun setReminderHour(hour: Int) {
        settings.reminderHour = hour
        _state.value = read()
    }

    suspend fun resetDemoData() {
        seeder.reset()
        _state.value = read()
    }
}

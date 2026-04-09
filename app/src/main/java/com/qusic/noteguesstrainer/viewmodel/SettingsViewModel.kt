package com.qusic.noteguesstrainer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qusic.noteguesstrainer.data.AppRepository
import com.qusic.noteguesstrainer.model.NoteLabelMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val volume: Float = 0.8f,
    val darkMode: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val noteLabelMode: NoteLabelMode = NoteLabelMode.LETTER_ONLY,
    val unlimitedReplay: Boolean = true,
    val allowAccidentals: Boolean = false,
)

class SettingsViewModel(
    private val repository: AppRepository,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = repository.appState.map { snapshot ->
        SettingsUiState(
            volume = snapshot.settings.volume,
            darkMode = snapshot.settings.darkMode,
            vibrationEnabled = snapshot.settings.vibrationEnabled,
            noteLabelMode = snapshot.settings.noteLabelMode,
            unlimitedReplay = snapshot.settings.unlimitedReplay,
            allowAccidentals = snapshot.settings.allowAccidentals,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun setVolume(value: Float) {
        viewModelScope.launch {
            repository.updateSettings { settings ->
                settings.copy(volume = value.coerceIn(0f, 1f))
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { settings -> settings.copy(darkMode = enabled) }
        }
    }

    fun setVibration(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { settings -> settings.copy(vibrationEnabled = enabled) }
        }
    }

    fun setUnlimitedReplay(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { settings -> settings.copy(unlimitedReplay = enabled) }
        }
    }

    fun setAllowAccidentals(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { settings -> settings.copy(allowAccidentals = enabled) }
        }
    }

    fun setNoteLabelMode(mode: NoteLabelMode) {
        viewModelScope.launch {
            repository.updateSettings { settings -> settings.copy(noteLabelMode = mode) }
        }
    }
}

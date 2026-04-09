package com.qusic.noteguesstrainer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qusic.noteguesstrainer.data.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val totalGuesses: Int = 0,
    val correctGuesses: Int = 0,
    val wrongGuesses: Int = 0,
    val accuracyPercent: Int = 0,
    val bestStreak: Int = 0,
    val currentStreak: Int = 0,
    val totalUnlockedNotes: Int = 2,
    val sessionsPlayed: Int = 0,
)

class StatsViewModel(
    repository: AppRepository,
) : ViewModel() {
    val uiState: StateFlow<StatsUiState> = repository.appState.map { snapshot ->
        StatsUiState(
            totalGuesses = snapshot.stats.totalGuesses,
            correctGuesses = snapshot.stats.correctGuesses,
            wrongGuesses = snapshot.stats.wrongGuesses,
            accuracyPercent = snapshot.stats.accuracyPercent,
            bestStreak = snapshot.stats.bestStreak,
            currentStreak = snapshot.progress.currentStreak,
            totalUnlockedNotes = snapshot.progress.unlockedNotes.size,
            sessionsPlayed = snapshot.stats.sessionsPlayed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )
}

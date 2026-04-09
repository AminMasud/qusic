package com.qusic.noteguesstrainer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qusic.noteguesstrainer.data.AppRepository
import com.qusic.noteguesstrainer.data.NoteCatalog
import com.qusic.noteguesstrainer.model.NoteId
import com.qusic.noteguesstrainer.model.NoteLabelMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ProgressUiState(
    val unlockedNotes: List<NoteId> = NoteCatalog.initialUnlocked,
    val lockedNotes: List<NoteId> = NoteCatalog.progressionOrder.drop(NoteCatalog.initialUnlocked.size),
    val currentStreak: Int = 0,
    val nextUnlock: NoteId? = NoteCatalog.nextUnlock(NoteCatalog.initialUnlocked),
    val nextUnlockRequirement: Int = NoteCatalog.unlockTargetStreak,
    val currentRange: String = NoteCatalog.displayRange(NoteCatalog.initialUnlocked),
    val currentOctaves: String = NoteCatalog.formatOctaves(NoteCatalog.initialUnlocked),
    val totalUnlockedNotes: Int = NoteCatalog.initialUnlocked.size,
    val labelMode: NoteLabelMode = NoteLabelMode.LETTER_ONLY,
    val forceOctaveLabels: Boolean = false,
)

class ProgressViewModel(
    repository: AppRepository,
) : ViewModel() {
    val uiState: StateFlow<ProgressUiState> = repository.appState.map { snapshot ->
        val unlockedNotes = snapshot.progress.unlockedNotes
        ProgressUiState(
            unlockedNotes = unlockedNotes,
            lockedNotes = NoteCatalog.progressionOrder.filterNot { note -> note in unlockedNotes },
            currentStreak = snapshot.progress.currentStreak,
            nextUnlock = NoteCatalog.nextUnlock(unlockedNotes),
            currentRange = NoteCatalog.displayRange(unlockedNotes),
            currentOctaves = NoteCatalog.formatOctaves(unlockedNotes),
            totalUnlockedNotes = unlockedNotes.size,
            labelMode = snapshot.settings.noteLabelMode,
            forceOctaveLabels = NoteCatalog.requiresOctaveLabels(
                unlockedNotes = unlockedNotes,
                mode = snapshot.settings.noteLabelMode,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressUiState(),
    )
}

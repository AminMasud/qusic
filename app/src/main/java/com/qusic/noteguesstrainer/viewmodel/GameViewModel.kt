package com.qusic.noteguesstrainer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qusic.noteguesstrainer.audio.NotePlayer
import com.qusic.noteguesstrainer.data.AppRepository
import com.qusic.noteguesstrainer.data.NoteCatalog
import com.qusic.noteguesstrainer.model.NoteId
import com.qusic.noteguesstrainer.model.NoteLabelMode
import com.qusic.noteguesstrainer.model.RoundFeedback
import com.qusic.noteguesstrainer.model.fullLabel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val isLoading: Boolean = true,
    val currentRoundNote: NoteId? = null,
    val unlockedNotes: List<NoteId> = NoteCatalog.initialUnlocked,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalGuesses: Int = 0,
    val accuracyPercent: Int = 0,
    val practicePreviewEnabled: Boolean = true,
    val practicePreviewRemainingCorrectAnswers: Int = NoteCatalog.practicePreviewCorrectLimit,
    val nextUnlock: NoteId? = NoteCatalog.nextUnlock(NoteCatalog.initialUnlocked),
    val unlockedRange: String = NoteCatalog.displayRange(NoteCatalog.initialUnlocked),
    val progressToUnlock: Float = 0f,
    val feedback: RoundFeedback? = null,
    val unlockDialogNote: NoteId? = null,
    val labelMode: NoteLabelMode = NoteLabelMode.LETTER_ONLY,
    val forceOctaveLabels: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val volume: Float = 0.8f,
    val unlimitedReplay: Boolean = true,
    val replayCount: Int = 0,
    val remainingReplays: Int? = null,
    val canAnswer: Boolean = false,
    val canReplay: Boolean = false,
    val canPreviewChoices: Boolean = false,
)

private data class GameTransientState(
    val feedback: RoundFeedback? = null,
    val unlockDialogNote: NoteId? = null,
    val replayCount: Int = 0,
    val isBusy: Boolean = false,
)

class GameViewModel(
    private val repository: AppRepository,
    private val notePlayer: NotePlayer,
) : ViewModel() {
    private val transientState = MutableStateFlow(GameTransientState())
    private var pendingPlaybackJob: Job? = null

    val uiState: StateFlow<GameUiState> = combine(
        repository.appState,
        transientState,
    ) { snapshot, transient ->
        val forceOctaveLabels = NoteCatalog.requiresOctaveLabels(
            unlockedNotes = snapshot.progress.unlockedNotes,
            mode = snapshot.settings.noteLabelMode,
        )
        val remainingReplays = if (snapshot.settings.unlimitedReplay) {
            null
        } else {
            (NoteCatalog.limitedReplayCount - transient.replayCount).coerceAtLeast(0)
        }
        val practicePreviewEnabled = NoteCatalog.isPracticePreviewEnabled(snapshot.stats.correctGuesses)

        GameUiState(
            isLoading = false,
            currentRoundNote = snapshot.progress.currentRoundNote,
            unlockedNotes = snapshot.progress.unlockedNotes,
            currentStreak = snapshot.progress.currentStreak,
            bestStreak = snapshot.stats.bestStreak,
            totalGuesses = snapshot.stats.totalGuesses,
            accuracyPercent = snapshot.stats.accuracyPercent,
            practicePreviewEnabled = practicePreviewEnabled,
            practicePreviewRemainingCorrectAnswers = NoteCatalog.remainingPracticePreviewCorrectAnswers(
                snapshot.stats.correctGuesses,
            ),
            nextUnlock = NoteCatalog.nextUnlock(snapshot.progress.unlockedNotes),
            unlockedRange = NoteCatalog.displayRange(snapshot.progress.unlockedNotes),
            progressToUnlock = (
                snapshot.progress.currentStreak.toFloat() /
                    NoteCatalog.unlockTargetStreak.toFloat()
                ).coerceIn(0f, 1f),
            feedback = transient.feedback,
            unlockDialogNote = transient.unlockDialogNote,
            labelMode = snapshot.settings.noteLabelMode,
            forceOctaveLabels = forceOctaveLabels,
            vibrationEnabled = snapshot.settings.vibrationEnabled,
            volume = snapshot.settings.volume,
            unlimitedReplay = snapshot.settings.unlimitedReplay,
            replayCount = transient.replayCount,
            remainingReplays = remainingReplays,
            canAnswer = snapshot.progress.currentRoundNote != null &&
                transient.feedback == null &&
                !transient.isBusy,
            canReplay = snapshot.progress.currentRoundNote != null &&
                (snapshot.settings.unlimitedReplay || transient.replayCount < NoteCatalog.limitedReplayCount),
            canPreviewChoices = practicePreviewEnabled &&
                snapshot.progress.currentRoundNote != null &&
                transient.feedback == null &&
                !transient.isBusy,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GameUiState(),
    )

    fun beginSession() {
        cancelPendingPlayback()
        transientState.update {
            it.copy(
                feedback = null,
                unlockDialogNote = null,
                replayCount = 0,
                isBusy = true,
            )
        }

        viewModelScope.launch {
            val snapshot = repository.beginSession()
            transientState.update { current -> current.copy(isBusy = false) }
            snapshot.progress.currentRoundNote?.let { note ->
                playNote(note, snapshot.settings.volume)
            }
        }
    }

    fun ensureRound() {
        if (uiState.value.currentRoundNote != null) {
            return
        }

        viewModelScope.launch {
            repository.ensureRound()
        }
    }

    fun replayCurrentNote() {
        val state = uiState.value
        val note = state.currentRoundNote ?: return
        if (!state.canReplay) {
            return
        }

        if (!state.unlimitedReplay) {
            transientState.update { current ->
                current.copy(replayCount = current.replayCount + 1)
            }
        }
        playNote(note, state.volume)
    }

    fun previewChoiceNote(note: NoteId) {
        val state = uiState.value
        if (!state.canPreviewChoices) {
            return
        }
        playNote(note, state.volume)
    }

    fun submitAnswer(selectedNote: NoteId) {
        val state = uiState.value
        if (!state.canAnswer) {
            return
        }

        cancelPendingPlayback()
        transientState.update { it.copy(isBusy = true) }

        viewModelScope.launch {
            val result = repository.submitAnswer(selectedNote)
            transientState.update { current ->
                current.copy(
                    isBusy = false,
                    replayCount = 0,
                    feedback = result.toFeedback(),
                    unlockDialogNote = result.unlockedNote,
                )
            }

            if (result.unlockedNote == null) {
                pendingPlaybackJob = viewModelScope.launch {
                    delay(1_050)
                    transientState.update { current ->
                        current.copy(feedback = null)
                    }
                    playNote(result.nextRoundNote, result.snapshot.settings.volume)
                }
            }
        }
    }

    fun dismissUnlockDialog() {
        val state = uiState.value
        transientState.update { current ->
            current.copy(
                feedback = null,
                unlockDialogNote = null,
            )
        }
        state.currentRoundNote?.let { note ->
            playNote(note, state.volume)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelPendingPlayback()
        notePlayer.release()
    }

    private fun playNote(
        note: NoteId,
        volume: Float,
    ) {
        notePlayer.play(note = note, volume = volume)
    }

    private fun cancelPendingPlayback() {
        pendingPlaybackJob?.cancel()
        pendingPlaybackJob = null
    }

    private fun com.qusic.noteguesstrainer.model.AnswerResult.toFeedback(): RoundFeedback {
        return if (isCorrect) {
            val messageParts = buildList {
                add("Correct!")
                if (unlockedNote != null) {
                    add("${unlockedNote.fullLabel()} is now unlocked.")
                }
                if (practicePreviewRemoved) {
                    add("Practice preview is now off. You will guess by ear only from here.")
                }
            }
            RoundFeedback(
                message = messageParts.joinToString(separator = " "),
                isCorrect = true,
                correctAnswer = correctAnswer,
                unlockedNote = unlockedNote,
            )
        } else {
            RoundFeedback(
                message = "Wrong. The answer was ${correctAnswer.fullLabel()}.",
                isCorrect = false,
                correctAnswer = correctAnswer,
            )
        }
    }
}

package com.qusic.noteguesstrainer.model

import kotlin.math.roundToInt

enum class NoteLabelMode {
    LETTER_ONLY,
    LETTER_AND_OCTAVE,
}

data class AppSettings(
    val volume: Float = 0.8f,
    val darkMode: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val noteLabelMode: NoteLabelMode = NoteLabelMode.LETTER_ONLY,
    val unlimitedReplay: Boolean = true,
    val allowAccidentals: Boolean = false,
)

data class AppStats(
    val totalGuesses: Int = 0,
    val correctGuesses: Int = 0,
    val wrongGuesses: Int = 0,
    val sessionsPlayed: Int = 0,
    val bestStreak: Int = 0,
) {
    val accuracyPercent: Int
        get() = if (totalGuesses == 0) {
            0
        } else {
            ((correctGuesses.toFloat() / totalGuesses.toFloat()) * 100f).roundToInt()
        }
}

data class AppProgress(
    val unlockedNotes: List<NoteId> = listOf(NoteId.C4, NoteId.D4),
    val currentStreak: Int = 0,
    val currentRoundNote: NoteId? = null,
    val previousRoundNote: NoteId? = null,
    val lastPlayedAtEpochMillis: Long = 0L,
)

data class AppSnapshot(
    val progress: AppProgress = AppProgress(),
    val stats: AppStats = AppStats(),
    val settings: AppSettings = AppSettings(),
)

data class RoundFeedback(
    val message: String,
    val isCorrect: Boolean,
    val correctAnswer: NoteId,
    val unlockedNote: NoteId? = null,
)

data class AnswerResult(
    val snapshot: AppSnapshot,
    val isCorrect: Boolean,
    val correctAnswer: NoteId,
    val unlockedNote: NoteId? = null,
    val nextRoundNote: NoteId,
)

package com.qusic.noteguesstrainer.data

import com.qusic.noteguesstrainer.model.NoteId
import com.qusic.noteguesstrainer.model.NoteLabelMode
import com.qusic.noteguesstrainer.model.fullLabel

object NoteCatalog {
    const val unlockTargetStreak = 10
    const val practicePreviewCorrectLimit = 6
    const val limitedReplayCount = 3

    val progressionOrder: List<NoteId> = NoteId.entries
    val initialUnlocked: List<NoteId> = progressionOrder.take(2)

    fun nextUnlock(unlockedNotes: List<NoteId>): NoteId? {
        return progressionOrder.firstOrNull { note -> note !in unlockedNotes }
    }

    fun isPracticePreviewEnabled(correctGuesses: Int): Boolean {
        return correctGuesses < practicePreviewCorrectLimit
    }

    fun remainingPracticePreviewCorrectAnswers(correctGuesses: Int): Int {
        return (practicePreviewCorrectLimit - correctGuesses).coerceAtLeast(0)
    }

    fun normalizeUnlocked(rawIds: Set<String>): List<NoteId> {
        val normalized = progressionOrder.filter { note -> note.name in rawIds }
        return if (normalized.size >= 2) normalized else initialUnlocked
    }

    fun activeOctaves(unlockedNotes: List<NoteId>): List<Int> {
        return unlockedNotes.map { note -> note.octave }.distinct().sorted()
    }

    fun displayRange(unlockedNotes: List<NoteId>): String {
        val safeList = if (unlockedNotes.isEmpty()) initialUnlocked else unlockedNotes
        return "${safeList.first().fullLabel()} - ${safeList.last().fullLabel()}"
    }

    fun requiresOctaveLabels(
        unlockedNotes: List<NoteId>,
        mode: NoteLabelMode,
    ): Boolean {
        if (mode == NoteLabelMode.LETTER_AND_OCTAVE) {
            return true
        }
        return unlockedNotes.groupingBy { note -> note.letter }.eachCount().any { it.value > 1 }
    }

    fun formatOctaves(unlockedNotes: List<NoteId>): String {
        return activeOctaves(unlockedNotes).joinToString(separator = ", ") { octave -> "Octave $octave" }
    }
}

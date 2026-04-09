package com.qusic.noteguesstrainer.data

import com.qusic.noteguesstrainer.model.AnswerResult
import com.qusic.noteguesstrainer.model.AppSnapshot
import com.qusic.noteguesstrainer.model.NoteId
import kotlin.random.Random

class GameEngine(
    private val random: Random = Random.Default,
) {
    fun createRound(
        unlockedNotes: List<NoteId>,
        previousRoundNote: NoteId?,
    ): NoteId {
        val notes = if (unlockedNotes.isEmpty()) NoteCatalog.initialUnlocked else unlockedNotes
        if (notes.size == 1) {
            return notes.first()
        }

        val antiRepeatPool = previousRoundNote?.let { previous ->
            notes.filterNot { note -> note == previous }
        }.orEmpty()

        val selectionPool = if (antiRepeatPool.isNotEmpty()) antiRepeatPool else notes
        return selectionPool[random.nextInt(selectionPool.size)]
    }

    fun answer(
        snapshot: AppSnapshot,
        selectedNote: NoteId,
        playedAtEpochMillis: Long = System.currentTimeMillis(),
    ): AnswerResult {
        val currentNote = requireNotNull(snapshot.progress.currentRoundNote) {
            "Cannot answer before a round has been created."
        }

        val isCorrect = selectedNote == currentNote
        val streakBeforeUnlock = if (isCorrect) snapshot.progress.currentStreak + 1 else 0
        val unlockedNote = if (isCorrect && streakBeforeUnlock == NoteCatalog.unlockTargetStreak) {
            NoteCatalog.nextUnlock(snapshot.progress.unlockedNotes)
        } else {
            null
        }
        val updatedUnlockedNotes = if (unlockedNote != null) {
            snapshot.progress.unlockedNotes + unlockedNote
        } else {
            snapshot.progress.unlockedNotes
        }
        val updatedStreak = when {
            !isCorrect -> 0
            unlockedNote != null -> 0
            else -> streakBeforeUnlock
        }
        val updatedStats = snapshot.stats.copy(
            totalGuesses = snapshot.stats.totalGuesses + 1,
            correctGuesses = snapshot.stats.correctGuesses + if (isCorrect) 1 else 0,
            wrongGuesses = snapshot.stats.wrongGuesses + if (isCorrect) 0 else 1,
            bestStreak = maxOf(snapshot.stats.bestStreak, streakBeforeUnlock),
        )
        val nextRoundNote = createRound(updatedUnlockedNotes, currentNote)
        val updatedProgress = snapshot.progress.copy(
            unlockedNotes = updatedUnlockedNotes,
            currentStreak = updatedStreak,
            currentRoundNote = nextRoundNote,
            previousRoundNote = currentNote,
            lastPlayedAtEpochMillis = playedAtEpochMillis,
        )

        return AnswerResult(
            snapshot = snapshot.copy(progress = updatedProgress, stats = updatedStats),
            isCorrect = isCorrect,
            correctAnswer = currentNote,
            unlockedNote = unlockedNote,
            nextRoundNote = nextRoundNote,
        )
    }
}

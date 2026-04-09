package com.qusic.noteguesstrainer

import com.qusic.noteguesstrainer.data.GameEngine
import com.qusic.noteguesstrainer.data.NoteCatalog
import com.qusic.noteguesstrainer.model.AppProgress
import com.qusic.noteguesstrainer.model.AppSnapshot
import com.qusic.noteguesstrainer.model.AppStats
import com.qusic.noteguesstrainer.model.NoteId
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {
    private val engine = GameEngine(random = Random(7))

    @Test
    fun correctAnswerIncrementsStreakAndStats() {
        val snapshot = AppSnapshot(
            progress = AppProgress(
                unlockedNotes = NoteCatalog.initialUnlocked,
                currentStreak = 3,
                currentRoundNote = NoteId.C4,
                previousRoundNote = NoteId.D4,
            ),
            stats = AppStats(totalGuesses = 4, correctGuesses = 3, wrongGuesses = 1, bestStreak = 3),
        )

        val result = engine.answer(snapshot, selectedNote = NoteId.C4, playedAtEpochMillis = 10L)

        assertTrue(result.isCorrect)
        assertEquals(4, result.snapshot.progress.currentStreak)
        assertEquals(5, result.snapshot.stats.totalGuesses)
        assertEquals(4, result.snapshot.stats.correctGuesses)
        assertEquals(4, result.snapshot.stats.bestStreak)
    }

    @Test
    fun wrongAnswerResetsStreakAndUpdatesWrongGuessCount() {
        val snapshot = AppSnapshot(
            progress = AppProgress(
                unlockedNotes = NoteCatalog.initialUnlocked,
                currentStreak = 9,
                currentRoundNote = NoteId.D4,
            ),
            stats = AppStats(totalGuesses = 9, correctGuesses = 9, wrongGuesses = 0, bestStreak = 9),
        )

        val result = engine.answer(snapshot, selectedNote = NoteId.C4, playedAtEpochMillis = 10L)

        assertFalse(result.isCorrect)
        assertEquals(0, result.snapshot.progress.currentStreak)
        assertEquals(10, result.snapshot.stats.totalGuesses)
        assertEquals(1, result.snapshot.stats.wrongGuesses)
        assertEquals(9, result.snapshot.stats.bestStreak)
    }

    @Test
    fun tenthCorrectAnswerUnlocksNextNoteAndResetsStreak() {
        val snapshot = AppSnapshot(
            progress = AppProgress(
                unlockedNotes = NoteCatalog.initialUnlocked,
                currentStreak = 9,
                currentRoundNote = NoteId.C4,
            ),
            stats = AppStats(totalGuesses = 9, correctGuesses = 9, wrongGuesses = 0, bestStreak = 9),
        )

        val result = engine.answer(snapshot, selectedNote = NoteId.C4, playedAtEpochMillis = 10L)

        assertTrue(result.isCorrect)
        assertEquals(NoteId.E4, result.unlockedNote)
        assertEquals(listOf(NoteId.C4, NoteId.D4, NoteId.E4), result.snapshot.progress.unlockedNotes)
        assertEquals(0, result.snapshot.progress.currentStreak)
        assertEquals(10, result.snapshot.stats.bestStreak)
    }

    @Test
    fun roundGenerationOnlyUsesUnlockedNotesAndAvoidsImmediateRepeat() {
        val unlocked = listOf(NoteId.C4, NoteId.D4, NoteId.E4)

        repeat(25) {
            val chosen = engine.createRound(unlockedNotes = unlocked, previousRoundNote = NoteId.D4)
            assertTrue(chosen in unlocked)
            assertNotEquals(NoteId.D4, chosen)
        }
    }
}

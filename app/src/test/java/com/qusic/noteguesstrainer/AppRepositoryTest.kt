package com.qusic.noteguesstrainer

import com.qusic.noteguesstrainer.data.AppRepository
import com.qusic.noteguesstrainer.data.GameEngine
import com.qusic.noteguesstrainer.model.AppProgress
import com.qusic.noteguesstrainer.model.AppSnapshot
import com.qusic.noteguesstrainer.model.AppStats
import com.qusic.noteguesstrainer.model.NoteId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppRepositoryTest {
    @Test
    fun beginSessionCreatesAndStoresRound() = runBlocking {
        val snapshotStore = FakeSnapshotStore()
        val repository = AppRepository(snapshotStore, GameEngine())

        val updated = repository.beginSession()

        assertNotNull(updated.progress.currentRoundNote)
        assertNotNull(snapshotStore.currentSnapshot().progress.currentRoundNote)
        assertEquals(1, updated.stats.sessionsPlayed)
    }

    @Test
    fun submitAnswerPersistsUnlockedNotesAndResetState() = runBlocking {
        val snapshotStore = FakeSnapshotStore(
            initialSnapshot = AppSnapshot(
                progress = AppProgress(
                    unlockedNotes = listOf(NoteId.C4, NoteId.D4),
                    currentStreak = 9,
                    currentRoundNote = NoteId.C4,
                ),
                stats = AppStats(totalGuesses = 9, correctGuesses = 9, wrongGuesses = 0, bestStreak = 9),
            ),
        )
        val repository = AppRepository(snapshotStore, GameEngine())

        repository.submitAnswer(NoteId.C4)

        val stored = snapshotStore.currentSnapshot()
        assertEquals(listOf(NoteId.C4, NoteId.D4, NoteId.E4), stored.progress.unlockedNotes)
        assertEquals(0, stored.progress.currentStreak)
        assertEquals(10, stored.stats.bestStreak)
    }
}

package com.qusic.noteguesstrainer.data

import com.qusic.noteguesstrainer.model.AnswerResult
import com.qusic.noteguesstrainer.model.AppSettings
import com.qusic.noteguesstrainer.model.AppSnapshot
import com.qusic.noteguesstrainer.model.NoteId
import com.qusic.noteguesstrainer.storage.SnapshotStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppRepository(
    private val snapshotStore: SnapshotStore,
    private val gameEngine: GameEngine,
) {
    private val mutex = Mutex()

    val appState: Flow<AppSnapshot> = snapshotStore.appState

    suspend fun beginSession(): AppSnapshot = mutex.withLock {
        snapshotStore.update { current ->
            val round = current.progress.currentRoundNote ?: gameEngine.createRound(
                unlockedNotes = current.progress.unlockedNotes,
                previousRoundNote = current.progress.previousRoundNote,
            )
            current.copy(
                progress = current.progress.copy(
                    currentRoundNote = round,
                    lastPlayedAtEpochMillis = System.currentTimeMillis(),
                ),
                stats = current.stats.copy(
                    sessionsPlayed = current.stats.sessionsPlayed + 1,
                ),
            )
        }
    }

    suspend fun ensureRound(): AppSnapshot = mutex.withLock {
        snapshotStore.update { current ->
            if (current.progress.currentRoundNote != null) {
                current
            } else {
                current.copy(
                    progress = current.progress.copy(
                        currentRoundNote = gameEngine.createRound(
                            unlockedNotes = current.progress.unlockedNotes,
                            previousRoundNote = current.progress.previousRoundNote,
                        ),
                        lastPlayedAtEpochMillis = System.currentTimeMillis(),
                    ),
                )
            }
        }
    }

    suspend fun submitAnswer(selectedNote: NoteId): AnswerResult = mutex.withLock {
        val currentSnapshot = snapshotStore.currentSnapshot()
        val preparedSnapshot = if (currentSnapshot.progress.currentRoundNote == null) {
            val ensured = currentSnapshot.copy(
                progress = currentSnapshot.progress.copy(
                    currentRoundNote = gameEngine.createRound(
                        unlockedNotes = currentSnapshot.progress.unlockedNotes,
                        previousRoundNote = currentSnapshot.progress.previousRoundNote,
                    ),
                    lastPlayedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
            snapshotStore.update { ensured }
            ensured
        } else {
            currentSnapshot
        }

        val result = gameEngine.answer(preparedSnapshot, selectedNote)
        snapshotStore.update { result.snapshot }
        result
    }

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings): AppSnapshot = mutex.withLock {
        snapshotStore.update { current ->
            current.copy(settings = transform(current.settings))
        }
    }
}

package com.qusic.noteguesstrainer.storage

import android.content.Context
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.qusic.noteguesstrainer.data.NoteCatalog
import com.qusic.noteguesstrainer.model.AppProgress
import com.qusic.noteguesstrainer.model.AppSettings
import com.qusic.noteguesstrainer.model.AppSnapshot
import com.qusic.noteguesstrainer.model.AppStats
import com.qusic.noteguesstrainer.model.NoteLabelMode
import com.qusic.noteguesstrainer.model.noteIdFromName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.appDataStore by preferencesDataStore(name = "note_guess_trainer_preferences")

class DataStoreManager(
    context: Context,
) : SnapshotStore {
    private val dataStore = context.appDataStore

    override val appState: Flow<AppSnapshot> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences -> preferences.toSnapshot() }

    override suspend fun currentSnapshot(): AppSnapshot = appState.first()

    override suspend fun update(transform: (AppSnapshot) -> AppSnapshot): AppSnapshot {
        var updatedSnapshot = AppSnapshot()
        dataStore.edit { preferences ->
            val currentSnapshot = preferences.toSnapshot()
            updatedSnapshot = transform(currentSnapshot).normalized()
            preferences.writeSnapshot(updatedSnapshot)
        }
        return updatedSnapshot
    }

    private object Keys {
        val unlockedNotes = stringSetPreferencesKey("unlocked_notes")
        val currentStreak = intPreferencesKey("current_streak")
        val currentRoundNote = stringPreferencesKey("current_round_note")
        val previousRoundNote = stringPreferencesKey("previous_round_note")
        val lastPlayedAt = longPreferencesKey("last_played_at")

        val totalGuesses = intPreferencesKey("total_guesses")
        val correctGuesses = intPreferencesKey("correct_guesses")
        val wrongGuesses = intPreferencesKey("wrong_guesses")
        val sessionsPlayed = intPreferencesKey("sessions_played")
        val bestStreak = intPreferencesKey("best_streak")

        val volume = floatPreferencesKey("volume")
        val darkMode = booleanPreferencesKey("dark_mode")
        val vibrationEnabled = booleanPreferencesKey("vibration_enabled")
        val noteLabelMode = stringPreferencesKey("note_label_mode")
        val unlimitedReplay = booleanPreferencesKey("unlimited_replay")
        val allowAccidentals = booleanPreferencesKey("allow_accidentals")
    }

    private fun androidx.datastore.preferences.core.Preferences.toSnapshot(): AppSnapshot {
        val unlockedNotes = NoteCatalog.normalizeUnlocked(this[Keys.unlockedNotes].orEmpty())
        val labelMode = this[Keys.noteLabelMode]?.let { raw ->
            runCatching { enumValueOf<NoteLabelMode>(raw) }.getOrNull()
        } ?: NoteLabelMode.LETTER_ONLY

        return AppSnapshot(
            progress = AppProgress(
                unlockedNotes = unlockedNotes,
                currentStreak = this[Keys.currentStreak] ?: 0,
                currentRoundNote = noteIdFromName(this[Keys.currentRoundNote]),
                previousRoundNote = noteIdFromName(this[Keys.previousRoundNote]),
                lastPlayedAtEpochMillis = this[Keys.lastPlayedAt] ?: 0L,
            ),
            stats = AppStats(
                totalGuesses = this[Keys.totalGuesses] ?: 0,
                correctGuesses = this[Keys.correctGuesses] ?: 0,
                wrongGuesses = this[Keys.wrongGuesses] ?: 0,
                sessionsPlayed = this[Keys.sessionsPlayed] ?: 0,
                bestStreak = this[Keys.bestStreak] ?: 0,
            ),
            settings = AppSettings(
                volume = (this[Keys.volume] ?: 0.8f).coerceIn(0f, 1f),
                darkMode = this[Keys.darkMode] ?: true,
                vibrationEnabled = this[Keys.vibrationEnabled] ?: true,
                noteLabelMode = labelMode,
                unlimitedReplay = this[Keys.unlimitedReplay] ?: true,
                allowAccidentals = this[Keys.allowAccidentals] ?: false,
            ),
        ).normalized()
    }

    private fun MutablePreferences.writeSnapshot(snapshot: AppSnapshot) {
        this[Keys.unlockedNotes] = snapshot.progress.unlockedNotes.map { note -> note.name }.toSet()
        this[Keys.currentStreak] = snapshot.progress.currentStreak
        snapshot.progress.currentRoundNote?.let { note -> this[Keys.currentRoundNote] = note.name }
            ?: remove(Keys.currentRoundNote)
        snapshot.progress.previousRoundNote?.let { note -> this[Keys.previousRoundNote] = note.name }
            ?: remove(Keys.previousRoundNote)
        this[Keys.lastPlayedAt] = snapshot.progress.lastPlayedAtEpochMillis

        this[Keys.totalGuesses] = snapshot.stats.totalGuesses
        this[Keys.correctGuesses] = snapshot.stats.correctGuesses
        this[Keys.wrongGuesses] = snapshot.stats.wrongGuesses
        this[Keys.sessionsPlayed] = snapshot.stats.sessionsPlayed
        this[Keys.bestStreak] = snapshot.stats.bestStreak

        this[Keys.volume] = snapshot.settings.volume.coerceIn(0f, 1f)
        this[Keys.darkMode] = snapshot.settings.darkMode
        this[Keys.vibrationEnabled] = snapshot.settings.vibrationEnabled
        this[Keys.noteLabelMode] = snapshot.settings.noteLabelMode.name
        this[Keys.unlimitedReplay] = snapshot.settings.unlimitedReplay
        this[Keys.allowAccidentals] = snapshot.settings.allowAccidentals
    }

    private fun AppSnapshot.normalized(): AppSnapshot {
        val normalizedUnlockedNotes = if (progress.unlockedNotes.size >= 2) {
            NoteCatalog.progressionOrder.filter { note -> note in progress.unlockedNotes }
        } else {
            NoteCatalog.initialUnlocked
        }
        return copy(
            progress = progress.copy(
                unlockedNotes = normalizedUnlockedNotes,
                currentStreak = progress.currentStreak.coerceAtLeast(0),
            ),
            settings = settings.copy(volume = settings.volume.coerceIn(0f, 1f)),
        )
    }
}

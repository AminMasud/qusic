package com.qusic.noteguesstrainer

import android.content.Context
import com.qusic.noteguesstrainer.audio.NotePlayer
import com.qusic.noteguesstrainer.audio.SynthNotePlayer
import com.qusic.noteguesstrainer.data.AppRepository
import com.qusic.noteguesstrainer.data.GameEngine
import com.qusic.noteguesstrainer.storage.DataStoreManager

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val snapshotStore = DataStoreManager(appContext)
    val gameEngine = GameEngine()
    val notePlayer: NotePlayer = SynthNotePlayer()
    val repository = AppRepository(snapshotStore = snapshotStore, gameEngine = gameEngine)
}

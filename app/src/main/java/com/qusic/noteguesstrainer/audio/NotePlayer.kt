package com.qusic.noteguesstrainer.audio

import com.qusic.noteguesstrainer.model.NoteId

interface NotePlayer {
    fun play(
        note: NoteId,
        volume: Float,
    )

    fun release()
}

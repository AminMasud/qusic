package com.qusic.noteguesstrainer.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.qusic.noteguesstrainer.model.NoteId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.PI
import kotlin.math.sin

class SynthNotePlayer : NotePlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val playbackMutex = Mutex()
    private val renderedNotes = mutableMapOf<NoteId, ShortArray>()
    private var activeTrack: AudioTrack? = null
    private var releaseJob: Job? = null

    override fun play(
        note: NoteId,
        volume: Float,
    ) {
        val pcm = renderedNotes.getOrPut(note) { renderNote(note.frequencyHz) }
        releaseJob?.cancel()

        scope.launch {
            val track = playbackMutex.withLock {
                activeTrack?.safeStopAndRelease()
                AudioTrack(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                    pcm.size * SHORT_BYTES,
                    AudioTrack.MODE_STATIC,
                    AudioManager.AUDIO_SESSION_ID_GENERATE,
                ).apply {
                    write(pcm, 0, pcm.size)
                    setVolume(volume.coerceIn(0f, 1f))
                    play()
                    activeTrack = this
                }
            }

            releaseJob = scope.launch {
                delay(NOTE_DURATION_MS.toLong())
                playbackMutex.withLock {
                    if (activeTrack == track) {
                        activeTrack?.safeStopAndRelease()
                        activeTrack = null
                    }
                }
            }
        }
    }

    override fun release() {
        releaseJob?.cancel()
        scope.launch {
            playbackMutex.withLock {
                activeTrack?.safeStopAndRelease()
                activeTrack = null
            }
        }
    }

    private fun renderNote(frequencyHz: Double): ShortArray {
        val sampleCount = SAMPLE_RATE * NOTE_DURATION_MS / 1000
        val attackSamples = SAMPLE_RATE / 80
        val releaseSamples = SAMPLE_RATE / 5
        val samples = ShortArray(sampleCount)

        for (sampleIndex in 0 until sampleCount) {
            val time = sampleIndex.toDouble() / SAMPLE_RATE.toDouble()
            val envelope = when {
                sampleIndex < attackSamples -> sampleIndex.toDouble() / attackSamples.toDouble()
                sampleIndex > sampleCount - releaseSamples -> {
                    ((sampleCount - sampleIndex).coerceAtLeast(0)).toDouble() / releaseSamples.toDouble()
                }
                else -> 1.0
            }
            val fundamental = sin(2.0 * PI * frequencyHz * time)
            val secondHarmonic = 0.32 * sin(2.0 * PI * frequencyHz * 2.0 * time)
            val thirdHarmonic = 0.12 * sin(2.0 * PI * frequencyHz * 3.0 * time)
            val tone = (fundamental + secondHarmonic + thirdHarmonic) * envelope * 0.38
            samples[sampleIndex] = (tone * Short.MAX_VALUE).toInt().toShort()
        }

        return samples
    }

    private fun AudioTrack.safeStopAndRelease() {
        runCatching {
            stop()
        }
        release()
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
        const val NOTE_DURATION_MS = 900
        const val SHORT_BYTES = 2
    }
}

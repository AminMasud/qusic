package com.qusic.noteguesstrainer.model

enum class NoteId(
    val letter: String,
    val octave: Int,
    val frequencyHz: Double,
) {
    C4(letter = "C", octave = 4, frequencyHz = 261.63),
    D4(letter = "D", octave = 4, frequencyHz = 293.66),
    E4(letter = "E", octave = 4, frequencyHz = 329.63),
    F4(letter = "F", octave = 4, frequencyHz = 349.23),
    G4(letter = "G", octave = 4, frequencyHz = 392.00),
    A4(letter = "A", octave = 4, frequencyHz = 440.00),
    B4(letter = "B", octave = 4, frequencyHz = 493.88),
    C5(letter = "C", octave = 5, frequencyHz = 523.25),
    D5(letter = "D", octave = 5, frequencyHz = 587.33),
    E5(letter = "E", octave = 5, frequencyHz = 659.25),
    F5(letter = "F", octave = 5, frequencyHz = 698.46),
    G5(letter = "G", octave = 5, frequencyHz = 783.99),
    A5(letter = "A", octave = 5, frequencyHz = 880.00),
    B5(letter = "B", octave = 5, frequencyHz = 987.77),
}

fun NoteId.fullLabel(): String = "$letter$octave"

fun NoteId.displayLabel(
    mode: NoteLabelMode,
    forceOctave: Boolean = false,
): String {
    return if (mode == NoteLabelMode.LETTER_ONLY && !forceOctave) {
        letter
    } else {
        fullLabel()
    }
}

fun noteIdFromName(raw: String?): NoteId? = raw?.let { value ->
    NoteId.entries.firstOrNull { it.name == value }
}

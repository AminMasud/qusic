# Note Guess Trainer

Note Guess Trainer is an Android ear-training game for beginners. The app plays a note, lets the player guess from the currently unlocked choices, and unlocks the next note every time the player reaches 10 correct answers in a row.

## Features

- Kotlin + Jetpack Compose Android app
- Beginner-friendly progression starting with `C4` and `D4`
- Local offline persistence with Preferences DataStore
- Synthesized note playback with instant replay support
- Home, Game, Progress, Stats, and Settings screens
- Streak-based unlocks, accuracy tracking, and session stats
- Unit tests for core progression logic and Compose UI tests for key screens

## Stack

- Kotlin
- Jetpack Compose + Material 3
- MVVM
- Navigation Compose
- Preferences DataStore
- Custom synthesized note player with `AudioTrack`

## How To Run

1. Open the project in Android Studio.
2. Make sure the Android SDK and build tools for API 36 are installed.
3. Sync Gradle.
4. Run the `app` configuration on an Android device or emulator.

From the command line on Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Folder Structure

```text
app/src/main/java/com/qusic/noteguesstrainer
|- audio/
|- data/
|- model/
|- navigation/
|- storage/
|- ui/
|- viewmodel/
|- MainActivity.kt
|- NoteGuessTrainerApplication.kt
```

## Gameplay Notes

- The first launch starts with `C4` and `D4` unlocked.
- Every correct answer increases the streak.
- A wrong answer resets the streak to `0`.
- At `10` consecutive correct answers, the next note in the progression unlocks.
- After the first octave is completed, the app continues unlocking notes in octave 5.

## Future Improvements

- Sharps and flats
- Alternate instrument sound packs
- Timed and endless modes
- Mixed-octave challenge presets
- Achievements and daily challenges

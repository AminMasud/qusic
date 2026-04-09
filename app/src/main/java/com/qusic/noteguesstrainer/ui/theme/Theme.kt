package com.qusic.noteguesstrainer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = SoftTeal,
    onPrimary = DeepInk,
    secondary = WarmGold,
    tertiary = Coral,
    background = DeepInk,
    surface = Slate,
    onSurface = Mist,
    onBackground = Mist,
)

private val LightColors = lightColorScheme(
    primary = DeepTeal,
    onPrimary = Mist,
    secondary = WarmGold,
    tertiary = Coral,
    background = Cream,
    surface = Mist,
    onSurface = DeepInk,
    onBackground = DeepInk,
)

@Composable
fun NoteGuessTrainerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}

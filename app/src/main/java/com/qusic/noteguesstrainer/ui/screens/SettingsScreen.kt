package com.qusic.noteguesstrainer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.qusic.noteguesstrainer.model.NoteLabelMode
import com.qusic.noteguesstrainer.viewmodel.SettingsUiState

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onVolumeChanged: (Float) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
    onVibrationChanged: (Boolean) -> Unit,
    onUnlimitedReplayChanged: (Boolean) -> Unit,
    onAccidentalsChanged: (Boolean) -> Unit,
    onLabelModeChanged: (NoteLabelMode) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingCard(title = "Volume") {
            Text(
                text = "${(uiState.volume * 100f).toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
            )
            Slider(
                value = uiState.volume,
                onValueChange = onVolumeChanged,
                modifier = Modifier.testTag("settings_volume"),
            )
        }

        SettingCard(title = "Replay") {
            SettingSwitchRow(
                title = "Unlimited replay",
                subtitle = "Turn this off to cap replays at 3 per round.",
                checked = uiState.unlimitedReplay,
                onCheckedChange = onUnlimitedReplayChanged,
                modifier = Modifier.testTag("settings_unlimited_replay"),
            )
        }

        SettingCard(title = "Note Labels") {
            Text(
                text = "Letter-only mode automatically shows octaves once multiple octaves are unlocked.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = uiState.noteLabelMode == NoteLabelMode.LETTER_ONLY,
                    onClick = { onLabelModeChanged(NoteLabelMode.LETTER_ONLY) },
                    label = { Text("C, D, E") },
                    modifier = Modifier.testTag("settings_label_letter"),
                )
                FilterChip(
                    selected = uiState.noteLabelMode == NoteLabelMode.LETTER_AND_OCTAVE,
                    onClick = { onLabelModeChanged(NoteLabelMode.LETTER_AND_OCTAVE) },
                    label = { Text("C4, D4, E4") },
                    modifier = Modifier.testTag("settings_label_full"),
                )
            }
        }

        SettingCard(title = "Appearance") {
            SettingSwitchRow(
                title = "Dark mode",
                subtitle = "Use the calm dark palette by default.",
                checked = uiState.darkMode,
                onCheckedChange = onDarkModeChanged,
                modifier = Modifier.testTag("settings_dark_mode"),
            )
        }

        SettingCard(title = "Feedback") {
            SettingSwitchRow(
                title = "Vibration",
                subtitle = "Use haptics for answer feedback and unlocks.",
                checked = uiState.vibrationEnabled,
                onCheckedChange = onVibrationChanged,
                modifier = Modifier.testTag("settings_vibration"),
            )
        }

        SettingCard(title = "Future Expansion") {
            SettingSwitchRow(
                title = "Accidentals",
                subtitle = "Reserved for a future sharp/flat mode.",
                checked = uiState.allowAccidentals,
                onCheckedChange = onAccidentalsChanged,
                modifier = Modifier.testTag("settings_accidentals"),
            )
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

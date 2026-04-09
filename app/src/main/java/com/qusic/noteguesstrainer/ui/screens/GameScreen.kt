package com.qusic.noteguesstrainer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qusic.noteguesstrainer.data.NoteCatalog
import com.qusic.noteguesstrainer.model.NoteId
import com.qusic.noteguesstrainer.model.displayLabel
import com.qusic.noteguesstrainer.model.fullLabel
import com.qusic.noteguesstrainer.viewmodel.GameUiState

@Composable
fun GameScreen(
    uiState: GameUiState,
    onPlayNote: () -> Unit,
    onReplayNote: () -> Unit,
    onAnswerSelected: (NoteId) -> Unit,
    onDismissUnlockDialog: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(uiState.feedback?.message) {
        if (uiState.feedback != null && uiState.vibrationEnabled) {
            haptics.performHapticFeedback(
                if (uiState.feedback.isCorrect) HapticFeedbackType.Confirm else HapticFeedbackType.LongPress
            )
        }
    }

    if (uiState.unlockDialogNote != null) {
        AlertDialog(
            onDismissRequest = onDismissUnlockDialog,
            confirmButton = {
                TextButton(onClick = onDismissUnlockDialog) {
                    Text("Keep Training")
                }
            },
            title = { Text("New Note Unlocked") },
            text = {
                Text("Nice work. ${uiState.unlockDialogNote.fullLabel()} has been added to your note pool.")
            },
            modifier = Modifier.testTag("unlock_dialog"),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Current range: ${uiState.unlockedRange}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Streak: ${uiState.currentStreak}/${NoteCatalog.unlockTargetStreak}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                LinearProgressIndicator(
                    progress = { uiState.progressToUnlock },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = uiState.nextUnlock?.let { "Next unlock target: ${it.fullLabel()}" }
                        ?: "You have unlocked every note in the MVP set.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onPlayNote,
                enabled = uiState.canReplay,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("game_play_note"),
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text("Play Note")
            }
            OutlinedButton(
                onClick = onReplayNote,
                enabled = uiState.canReplay,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("game_replay_note"),
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text("Replay Note")
            }
        }

        if (!uiState.unlimitedReplay) {
            Text(
                text = "Replays left this round: ${uiState.remainingReplays ?: 0}",
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        uiState.feedback?.let { feedback ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("feedback_banner"),
                color = if (feedback.isCorrect) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                },
                shape = MaterialTheme.shapes.large,
            ) {
                Text(
                    text = feedback.message,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Choose the note you hear",
                    style = MaterialTheme.typography.titleLarge,
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("answer_grid"),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    uiState.unlockedNotes.forEach { note ->
                        OutlinedButton(
                            onClick = { onAnswerSelected(note) },
                            enabled = uiState.canAnswer,
                            modifier = Modifier
                                .widthIn(min = 112.dp)
                                .testTag("answer_${note.name}"),
                        ) {
                            Text(note.displayLabel(uiState.labelMode, uiState.forceOctaveLabels))
                        }
                    }
                }
            }
        }

        if (uiState.currentRoundNote == null) {
            Text(
                text = "Preparing your next note...",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

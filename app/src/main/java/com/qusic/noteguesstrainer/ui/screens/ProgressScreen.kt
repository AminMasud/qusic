package com.qusic.noteguesstrainer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qusic.noteguesstrainer.model.displayLabel
import com.qusic.noteguesstrainer.model.fullLabel
import com.qusic.noteguesstrainer.viewmodel.ProgressUiState

@Composable
fun ProgressScreen(
    uiState: ProgressUiState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Unlocked notes: ${uiState.totalUnlockedNotes}", style = MaterialTheme.typography.titleLarge)
                Text("Current streak: ${uiState.currentStreak}/${uiState.nextUnlockRequirement}", style = MaterialTheme.typography.bodyLarge)
                Text("Range: ${uiState.currentRange}", style = MaterialTheme.typography.bodyLarge)
                Text("Active octaves: ${uiState.currentOctaves}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = uiState.nextUnlock?.let { "Next unlock: ${it.fullLabel()}" }
                        ?: "All currently planned notes are unlocked.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Unlocked", style = MaterialTheme.typography.titleLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    uiState.unlockedNotes.forEach { note ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(note.displayLabel(uiState.labelMode, uiState.forceOctaveLabels))
                            },
                        )
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Coming Next", style = MaterialTheme.typography.titleLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    uiState.lockedNotes.forEach { note ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(note.displayLabel(uiState.labelMode, forceOctave = true))
                            },
                        )
                    }
                }
            }
        }
    }
}

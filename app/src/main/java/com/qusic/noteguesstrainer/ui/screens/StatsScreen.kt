package com.qusic.noteguesstrainer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qusic.noteguesstrainer.viewmodel.StatsUiState

@Composable
fun StatsScreen(
    uiState: StatsUiState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        StatCard(title = "Total Guesses", value = uiState.totalGuesses.toString())
        StatCard(title = "Correct Guesses", value = uiState.correctGuesses.toString())
        StatCard(title = "Wrong Guesses", value = uiState.wrongGuesses.toString())
        StatCard(
            title = "Overall Accuracy",
            value = "${uiState.accuracyPercent}%",
            modifier = Modifier.testTag("stats_accuracy"),
        )
        StatCard(title = "Best Streak", value = uiState.bestStreak.toString())
        StatCard(title = "Current Streak", value = uiState.currentStreak.toString())
        StatCard(title = "Unlocked Notes", value = uiState.totalUnlockedNotes.toString())
        StatCard(title = "Play Sessions", value = uiState.sessionsPlayed.toString())
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

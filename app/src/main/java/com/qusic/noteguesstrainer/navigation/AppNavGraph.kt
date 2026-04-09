package com.qusic.noteguesstrainer.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.qusic.noteguesstrainer.data.NoteCatalog
import com.qusic.noteguesstrainer.ui.screens.GameScreen
import com.qusic.noteguesstrainer.ui.screens.HomeScreen
import com.qusic.noteguesstrainer.ui.screens.ProgressScreen
import com.qusic.noteguesstrainer.ui.screens.SettingsScreen
import com.qusic.noteguesstrainer.ui.screens.StatsScreen
import com.qusic.noteguesstrainer.viewmodel.GameViewModel
import com.qusic.noteguesstrainer.viewmodel.ProgressViewModel
import com.qusic.noteguesstrainer.viewmodel.SettingsViewModel
import com.qusic.noteguesstrainer.viewmodel.StatsViewModel

@Composable
fun AppNavGraph(
    gameViewModel: GameViewModel,
    progressViewModel: ProgressViewModel,
    statsViewModel: StatsViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val progressState by progressViewModel.uiState.collectAsStateWithLifecycle()
    val gameState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val statsState by statsViewModel.uiState.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppDestination.HOME.route
    val currentDestination = AppDestination.entries.firstOrNull { it.route == currentRoute } ?: AppDestination.HOME

    AppScaffold(
        currentDestination = currentDestination,
        canNavigateBack = navController.previousBackStackEntry != null,
        onNavigateBack = navController::navigateUp,
        modifier = modifier,
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.HOME.route,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable(AppDestination.HOME.route) {
                HomeScreen(
                    unlockedCount = progressState.totalUnlockedNotes,
                    totalNotes = NoteCatalog.progressionOrder.size,
                    currentStreak = progressState.currentStreak,
                    nextUnlock = progressState.nextUnlock,
                    onPlay = {
                        gameViewModel.beginSession()
                        navController.navigate(AppDestination.GAME.route)
                    },
                    onOpenProgress = { navController.navigate(AppDestination.PROGRESS.route) },
                    onOpenStats = { navController.navigate(AppDestination.STATS.route) },
                    onOpenSettings = { navController.navigate(AppDestination.SETTINGS.route) },
                )
            }

            composable(AppDestination.GAME.route) {
                LaunchedEffect(Unit) {
                    gameViewModel.ensureRound()
                }
                GameScreen(
                    uiState = gameState,
                    onPlayNote = gameViewModel::replayCurrentNote,
                    onReplayNote = gameViewModel::replayCurrentNote,
                    onPreviewNote = gameViewModel::previewChoiceNote,
                    onAnswerSelected = gameViewModel::submitAnswer,
                    onDismissUnlockDialog = gameViewModel::dismissUnlockDialog,
                )
            }

            composable(AppDestination.PROGRESS.route) {
                ProgressScreen(uiState = progressState)
            }

            composable(AppDestination.STATS.route) {
                StatsScreen(uiState = statsState)
            }

            composable(AppDestination.SETTINGS.route) {
                SettingsScreen(
                    uiState = settingsState,
                    onVolumeChanged = settingsViewModel::setVolume,
                    onDarkModeChanged = settingsViewModel::setDarkMode,
                    onVibrationChanged = settingsViewModel::setVibration,
                    onUnlimitedReplayChanged = settingsViewModel::setUnlimitedReplay,
                    onAccidentalsChanged = settingsViewModel::setAllowAccidentals,
                    onLabelModeChanged = settingsViewModel::setNoteLabelMode,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    currentDestination: AppDestination,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            if (currentDestination != AppDestination.HOME) {
                TopAppBar(
                    title = { Text(currentDestination.title) },
                    navigationIcon = {
                        if (canNavigateBack) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                )
                            }
                        }
                    },
                )
            }
        },
        content = content,
    )
}

package com.qusic.noteguesstrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qusic.noteguesstrainer.navigation.AppNavGraph
import com.qusic.noteguesstrainer.ui.theme.NoteGuessTrainerTheme
import com.qusic.noteguesstrainer.viewmodel.AppViewModelFactory
import com.qusic.noteguesstrainer.viewmodel.GameViewModel
import com.qusic.noteguesstrainer.viewmodel.ProgressViewModel
import com.qusic.noteguesstrainer.viewmodel.SettingsViewModel
import com.qusic.noteguesstrainer.viewmodel.StatsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as NoteGuessTrainerApplication).appContainer
        val factory = AppViewModelFactory(appContainer)

        setContent {
            val gameViewModel: GameViewModel = viewModel(factory = factory)
            val progressViewModel: ProgressViewModel = viewModel(factory = factory)
            val statsViewModel: StatsViewModel = viewModel(factory = factory)
            val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            NoteGuessTrainerTheme(darkTheme = settingsState.darkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(
                        gameViewModel = gameViewModel,
                        progressViewModel = progressViewModel,
                        statsViewModel = statsViewModel,
                        settingsViewModel = settingsViewModel,
                    )
                }
            }
        }
    }
}

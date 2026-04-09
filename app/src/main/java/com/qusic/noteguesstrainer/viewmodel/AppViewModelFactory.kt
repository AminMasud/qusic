package com.qusic.noteguesstrainer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qusic.noteguesstrainer.AppContainer

class AppViewModelFactory(
    private val appContainer: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(GameViewModel::class.java) -> {
                GameViewModel(
                    repository = appContainer.repository,
                    notePlayer = appContainer.notePlayer,
                ) as T
            }

            modelClass.isAssignableFrom(ProgressViewModel::class.java) -> {
                ProgressViewModel(appContainer.repository) as T
            }

            modelClass.isAssignableFrom(StatsViewModel::class.java) -> {
                StatsViewModel(appContainer.repository) as T
            }

            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(appContainer.repository) as T
            }

            else -> error("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

package com.qusic.noteguesstrainer.storage

import com.qusic.noteguesstrainer.model.AppSnapshot
import kotlinx.coroutines.flow.Flow

interface SnapshotStore {
    val appState: Flow<AppSnapshot>

    suspend fun currentSnapshot(): AppSnapshot

    suspend fun update(transform: (AppSnapshot) -> AppSnapshot): AppSnapshot
}

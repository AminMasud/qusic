package com.qusic.noteguesstrainer

import com.qusic.noteguesstrainer.model.AppSnapshot
import com.qusic.noteguesstrainer.storage.SnapshotStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSnapshotStore(
    initialSnapshot: AppSnapshot = AppSnapshot(),
) : SnapshotStore {
    private val backingState = MutableStateFlow(initialSnapshot)

    override val appState: Flow<AppSnapshot> = backingState

    override suspend fun currentSnapshot(): AppSnapshot = backingState.value

    override suspend fun update(transform: (AppSnapshot) -> AppSnapshot): AppSnapshot {
        val updated = transform(backingState.value)
        backingState.value = updated
        return updated
    }
}

package com.example.composechatsample.core.repository

internal object NoOpSyncStateRepository : SyncStateRepository {
    override suspend fun insertSyncState(syncState: SyncState) { /* No-Op */ }
    override suspend fun selectSyncState(userId: String): SyncState? = null
    override suspend fun clear() { /* No-Op */ }
}
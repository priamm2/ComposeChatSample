package com.example.composechatsample.core.repository

public interface SyncStateRepository {

    public suspend fun insertSyncState(syncState: SyncState)

    public suspend fun selectSyncState(userId: String): SyncState?

    public suspend fun clear()
}
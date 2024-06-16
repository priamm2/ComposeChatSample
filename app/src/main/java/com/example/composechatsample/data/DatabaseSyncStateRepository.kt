package com.example.composechatsample.data

import com.example.composechatsample.core.repository.SyncState
import com.example.composechatsample.core.repository.SyncStateRepository

internal class DatabaseSyncStateRepository(private val syncStateDao: SyncStateDao) :
    SyncStateRepository {

    override suspend fun insertSyncState(syncState: SyncState) {
        syncStateDao.insert(syncState.toEntity())
    }

    override suspend fun selectSyncState(userId: String): SyncState? {
        return syncStateDao.select(userId)?.toModel()
    }

    override suspend fun clear() {
        syncStateDao.deleteAll()
    }
}
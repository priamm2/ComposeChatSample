package com.example.composechatsample.data

import androidx.room.TypeConverter
import com.example.composechatsample.core.models.SyncStatus

internal class SyncStatusConverter {
    @TypeConverter
    fun stringToSyncStatus(data: Int): SyncStatus {
        return SyncStatus.fromInt(data)!!
    }

    @TypeConverter
    fun syncStatusToString(syncStatus: SyncStatus): Int {
        return syncStatus.status
    }
}
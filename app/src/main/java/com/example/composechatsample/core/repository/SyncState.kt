package com.example.composechatsample.core.repository

import java.util.Date

data class SyncState(
    val userId: String,
    val activeChannelIds: List<String> = emptyList(),
    val lastSyncedAt: Date? = null,
    val rawLastSyncedAt: String? = null,
    val markedAllReadAt: Date? = null,
)

fun SyncState.stringify(): String {
    return "SyncState(userId='$userId', activeChannelIds.size=${activeChannelIds.size}, " +
            "lastSyncedAt=$lastSyncedAt, rawLastSyncedAt=$rawLastSyncedAt, markedAllReadAt=$markedAllReadAt)"
}
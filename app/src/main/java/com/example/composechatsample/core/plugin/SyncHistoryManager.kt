package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.events.ChatEvent
import kotlinx.coroutines.flow.Flow

internal interface SyncHistoryManager {

    val syncedEvents: Flow<List<ChatEvent>>
    fun start()
    suspend fun sync()
    suspend fun awaitSyncing()
    fun stop()
}
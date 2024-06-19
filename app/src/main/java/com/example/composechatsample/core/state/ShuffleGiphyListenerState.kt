package com.example.composechatsample.core.state

import com.example.composechatsample.core.Result
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.plugin.LogicRegistry
import com.example.composechatsample.core.plugin.ShuffleGiphyListener

internal class ShuffleGiphyListenerState(private val logic: LogicRegistry) : ShuffleGiphyListener {

    override suspend fun onShuffleGiphyResult(cid: String, result: Result<Message>) {
        if (result is Result.Success) {
            val processedMessage = result.value.copy(syncStatus = SyncStatus.COMPLETED)
            logic.channelFromMessage(processedMessage)?.upsertMessage(processedMessage)
            logic.threadFromMessage(processedMessage)?.upsertMessage(processedMessage)
        }
    }
}
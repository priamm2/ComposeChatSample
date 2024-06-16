package com.example.composechatsample.core.state

import android.content.Context
import com.example.composechatsample.log.taggedLogger

internal class OfflineSyncFirebaseMessagingHandler {
    private val logger by taggedLogger("Chat:OfflineSyncFirebaseMessagingHandler")

    fun syncMessages(context: Context, cid: String) {
        logger.d { "Starting the sync" }

        SyncMessagesWork.start(context, cid)
    }

    fun cancel(context: Context) {
        SyncMessagesWork.cancel(context)
    }
}
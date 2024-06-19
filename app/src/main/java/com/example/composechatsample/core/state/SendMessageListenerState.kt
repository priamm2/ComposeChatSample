package com.example.composechatsample.core.state

import com.example.composechatsample.core.enrichWithCid
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.plugin.LogicRegistry
import com.example.composechatsample.core.plugin.SendMessageListener
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.isPermanent
import com.example.composechatsample.log.StreamLog
import java.util.Date

private const val TAG = "Chat:SendMessageHandler"

internal class SendMessageListenerState(private val logic: LogicRegistry) : SendMessageListener {


    override suspend fun onMessageSendResult(
        result: Result<Message>,
        channelType: String,
        channelId: String,
        message: Message,
    ) {
        handleLastSentMessageDate(result, message)

        val cid = "$channelType:$channelId"

        if (logic.getMessageById(message.id)?.syncStatus == SyncStatus.COMPLETED) return

        when (result) {
            is Result.Success -> handleSendMessageSuccess(cid, logic, result.value)
            is Result.Failure -> handleSendMessageFailure(logic, message, result.value)
        }
    }

    private fun handleSendMessageSuccess(
        cid: String,
        logic: LogicRegistry,
        processedMessage: Message,
    ) {
        processedMessage.enrichWithCid(cid)
            .copy(syncStatus = SyncStatus.COMPLETED)
            .also { message ->
                logic.channelFromMessage(message)?.upsertMessage(message)
                logic.threadFromMessage(message)?.upsertMessage(message)
            }
    }

    private fun handleSendMessageFailure(
        logic: LogicRegistry,
        message: Message,
        error: Error,
    ) {
        val isPermanentError = error.isPermanent()
        StreamLog.w(TAG) { "[handleSendMessageFailure] isPermanentError: $isPermanentError" }
        message.copy(
            syncStatus = if (isPermanentError) {
                SyncStatus.FAILED_PERMANENTLY
            } else {
                SyncStatus.SYNC_NEEDED
            },
            updatedLocallyAt = Date(),
        ).also {
            logic.channelFromMessage(it)?.upsertMessage(it)
            logic.threadFromMessage(it)?.upsertMessage(it)
        }
    }

    private fun handleLastSentMessageDate(
        result: Result<Message>,
        message: Message,
    ) {
        if (result is Result.Success) {
            logic.channelFromMessage(message)?.setLastSentMessageDate(result.value.createdAt)
        }
    }
}
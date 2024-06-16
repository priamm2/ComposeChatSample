package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.enrichWithCid
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.repository.MessageRepository
import com.example.composechatsample.core.repository.UserRepository
import com.example.composechatsample.core.users
import com.example.composechatsample.log.StreamLog
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.isPermanent
import java.util.Date

private const val TAG = "Chat:SendMessageHandlerDB"

internal class SendMessageListenerDatabase(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
) : SendMessageListener {


    override suspend fun onMessageSendResult(
        result: Result<Message>,
        channelType: String,
        channelId: String,
        message: Message,
    ) {
        val cid = "$channelType:$channelId"
        if (messageRepository.selectMessage(message.id)?.syncStatus == SyncStatus.COMPLETED) return

        when (result) {
            is Result.Success -> handleSendMessageSuccess(cid, result.value)
            is Result.Failure -> handleSendMessageFailure(message, result.value)
        }
    }

    private suspend fun handleSendMessageSuccess(
        cid: String,
        processedMessage: Message,
    ) {
        processedMessage.enrichWithCid(cid)
            .copy(syncStatus = SyncStatus.COMPLETED)
            .also { message ->
                userRepository.insertUsers(message.users())
                messageRepository.insertMessage(message)
            }
    }

    private suspend fun handleSendMessageFailure(
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
        ).also { parsedMessage ->
            userRepository.insertUsers(parsedMessage.users())
            messageRepository.insertMessage(parsedMessage)
        }
    }
}
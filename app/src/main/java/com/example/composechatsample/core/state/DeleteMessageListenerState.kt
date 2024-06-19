package com.example.composechatsample.core.state

import com.example.composechatsample.core.errors.MessageModerationDeletedException
import com.example.composechatsample.core.isModerationError
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.plugin.ChannelLogic
import com.example.composechatsample.core.plugin.DeleteMessageListener
import com.example.composechatsample.core.plugin.GlobalState
import com.example.composechatsample.core.plugin.LogicRegistry
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.Result
import java.util.Date

internal class DeleteMessageListenerState(
    private val logic: LogicRegistry,
    private val clientState: ClientState,
    private val globalState: GlobalState,
) : DeleteMessageListener {


    override suspend fun onMessageDeletePrecondition(messageId: String): Result<Unit> {
        val channelLogic: ChannelLogic? = logic.channelFromMessageId(messageId)

        return channelLogic?.getMessage(messageId)?.let { message ->
            val isModerationFailed = message.isModerationError(clientState.user.value?.id)

            if (isModerationFailed) {
                deleteMessage(message)
                Result.Failure(
                    Error.ThrowableError(
                        message = "Message with failed moderation has been deleted locally: $messageId",
                        cause = MessageModerationDeletedException(
                            "Message with failed moderation has been deleted locally: $messageId",
                        ),
                    ),
                )
            } else {
                Result.Success(Unit)
            }
        } ?: Result.Failure(Error.GenericError(message = "No message found with id: $messageId"))
    }

    override suspend fun onMessageDeleteRequest(messageId: String) {
        val channelLogic: ChannelLogic? = logic.channelFromMessageId(messageId)

        channelLogic?.getMessage(messageId)?.let { message ->
            val isModerationFailed = message.isModerationError(clientState.user.value?.id)

            if (isModerationFailed) {
                deleteMessage(message)
            } else {
                val networkAvailable = clientState.isNetworkAvailable
                val messageToBeDeleted = message.copy(
                    deletedAt = Date(),
                    syncStatus = if (!networkAvailable) SyncStatus.SYNC_NEEDED else SyncStatus.IN_PROGRESS,
                )

                updateMessage(messageToBeDeleted)
            }
        }
    }


    override suspend fun onMessageDeleteResult(originalMessageId: String, result: Result<Message>) {
        when (result) {
            is Result.Success -> {
                updateMessage(result.value.copy(syncStatus = SyncStatus.COMPLETED))
            }
            is Result.Failure -> {
                logic.channelFromMessageId(originalMessageId)
                    ?.getMessage(originalMessageId)
                    ?.let { originalMessage ->
                        val failureMessage = originalMessage.copy(
                            syncStatus = SyncStatus.SYNC_NEEDED,
                            updatedLocallyAt = Date(),
                        )

                        updateMessage(failureMessage)
                    }
            }
        }
    }

    private fun updateMessage(message: Message) {
        logic.channelFromMessage(message)?.upsertMessage(message)
        logic.threadFromMessage(message)?.upsertMessage(message)
    }

    private fun deleteMessage(message: Message) {
        logic.channelFromMessage(message)?.deleteMessage(message)
        logic.threadFromMessage(message)?.deleteMessage(message)
    }
}
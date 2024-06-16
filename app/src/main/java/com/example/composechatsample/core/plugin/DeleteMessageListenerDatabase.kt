package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.Result
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.errors.MessageModerationDeletedException
import com.example.composechatsample.core.isModerationError
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.repository.MessageRepository
import com.example.composechatsample.core.repository.UserRepository
import com.example.composechatsample.core.state.ClientState
import com.example.composechatsample.core.users
import java.util.Date

internal class DeleteMessageListenerDatabase(
    private val clientState: ClientState,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
) : DeleteMessageListener {

    override suspend fun onMessageDeletePrecondition(messageId: String): Result<Unit> {
        return messageRepository.selectMessage(messageId)?.let { message ->
            val currentUserId = clientState.user.value?.id
            val isModerationFailed = message.isModerationError(currentUserId)

            if (isModerationFailed) {
                messageRepository.deleteChannelMessage(message)
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
        messageRepository.selectMessage(messageId)?.let { message ->
            val networkAvailable = clientState.isNetworkAvailable
            val messageToBeDeleted = message.copy(
                deletedAt = Date(),
                syncStatus = if (!networkAvailable) SyncStatus.SYNC_NEEDED else SyncStatus.IN_PROGRESS,
            )

            userRepository.insertUsers(messageToBeDeleted.users())
            messageRepository.insertMessage(messageToBeDeleted)
        }
    }

    override suspend fun onMessageDeleteResult(originalMessageId: String, result: Result<Message>) {
        when (result) {
            is Result.Success -> {
                messageRepository.insertMessage(
                    result.value.copy(syncStatus = SyncStatus.COMPLETED),
                )
            }
            is Result.Failure -> {
                messageRepository.selectMessage(originalMessageId)?.let { originalMessage ->
                    val failureMessage = originalMessage.copy(
                        syncStatus = SyncStatus.SYNC_NEEDED,
                        updatedLocallyAt = Date(),
                    )

                    messageRepository.insertMessage(failureMessage)
                }
            }
        }
    }
}
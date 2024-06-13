package com.example.composechatsample.core

import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.state.ClientState
import java.util.Date

internal class PrepareMessageLogicImpl(
    private val clientState: ClientState,
    private val channelStateLogicProvider: ChannelStateLogicProvider?,
) : PrepareMessageLogic {

    @Suppress("ComplexMethod")
    override fun prepareMessage(message: Message, channelId: String, channelType: String, user: User): Message {
        val channel = channelStateLogicProvider?.channelStateLogic(channelType, channelId)

        val attachments = message.attachments.map {
            when (it.upload) {
                null -> it.copy(uploadState = Attachment.UploadState.Success)
                else -> it.copy(
                    extraData = it.extraData + mapOf(EXTRA_UPLOAD_ID to (it.uploadId ?: generateUploadId())),
                    uploadState = Attachment.UploadState.Idle,
                )
            }
        }
        return message.ensureId(user).copy(
            user = user,
            attachments = attachments,
            type = getMessageType(message),
            createdLocallyAt = message.createdAt ?: message.createdLocallyAt ?: Date(),
            syncStatus = when {
                attachments.any { it.uploadState is Attachment.UploadState.Idle } -> SyncStatus.AWAITING_ATTACHMENTS
                clientState.isNetworkAvailable -> SyncStatus.IN_PROGRESS
                else -> SyncStatus.SYNC_NEEDED
            },
        )
            .let { copiedMessage ->
                copiedMessage.takeIf { it.cid.isBlank() }
                    ?.enrichWithCid("$channelType:$channelId")
                    ?: copiedMessage
            }
            .let { copiedMessage ->
                channel
                    ?.listenForChannelState()
                    ?.toChannel()
                    ?.let(copiedMessage::populateMentions)
                    ?: copiedMessage
            }
            .also { preparedMessage ->
                if (preparedMessage.replyMessageId != null) {
                    channel?.replyMessage(null)
                }
            }
    }

    private fun generateUploadId(): String {
        return "upload_id_${UUID.randomUUID()}"
    }
}
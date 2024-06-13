package com.example.composechatsample.core

import android.content.Context
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.UploadAttachmentsNetworkType
import com.example.composechatsample.core.state.ClientState
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import java.util.UUID

internal class AttachmentsSender(
    private val context: Context,
    private val networkType: UploadAttachmentsNetworkType,
    private val clientState: ClientState,
    private val scope: CoroutineScope,
    private val verifier: AttachmentsVerifier = AttachmentsVerifier,
) {

    private var jobsMap: Map<String, Job> = emptyMap()
    private val uploadIds = mutableMapOf<String, UUID>()
    private val logger by taggedLogger("Chat:AttachmentsSender")

    internal suspend fun sendAttachments(
        message: Message,
        channelType: String,
        channelId: String,
        isRetrying: Boolean,
    ): Result<Message> {
        val result = if (!isRetrying) {
            if (message.hasPendingAttachments()) {
                logger.d {
                    "[sendAttachments] Message ${message.id}" +
                        " has ${message.attachments.size} pending attachments"
                }
                uploadAttachments(message, channelType, channelId)
            } else {
                logger.d { "[sendAttachments] Message ${message.id} without attachments" }
                Result.Success(message)
            }
        } else {
            logger.d { "[sendAttachments] Retrying Message ${message.id}" }
            retryMessage(message, channelType, channelId)
        }
        return verifier.verifyAttachments(result)
    }

    private suspend fun retryMessage(
        message: Message,
        channelType: String,
        channelId: String,
    ): Result<Message> =
        uploadAttachments(message, channelType, channelId)

    private suspend fun uploadAttachments(
        message: Message,
        channelType: String,
        channelId: String,
    ): Result<Message> {
        return if (clientState.isNetworkAvailable) {
            waitForAttachmentsToBeSent(message, channelType, channelId)
        } else {
            enqueueAttachmentUpload(message, channelType, channelId)
            logger.d { "[uploadAttachments] Chat is offline, not sending message with id ${message.id}" }
            Result.Failure(
                Error.GenericError(
                    "Chat is offline, not sending message with id ${message.id} and text ${message.text}",
                ),
            )
        }
    }

    private suspend fun waitForAttachmentsToBeSent(
        newMessage: Message,
        channelType: String,
        channelId: String,
    ): Result<Message> {
        jobsMap[newMessage.id]?.cancel()
        var allAttachmentsUploaded = false
        var messageToBeSent = newMessage

        AttachmentsUploadStates.updateMessageAttachments(messageToBeSent)

        jobsMap = jobsMap + (
            newMessage.id to scope.launch {
                AttachmentsUploadStates.observeAttachments(newMessage.id)
                    .filterNot(Collection<Attachment>::isEmpty)
                    .collect { attachments ->
                        when {
                            attachments.all { it.uploadState == Attachment.UploadState.Success } -> {
                                messageToBeSent = newMessage.copy(attachments = attachments.toMutableList())
                                allAttachmentsUploaded = true
                                jobsMap[newMessage.id]?.cancel()
                            }
                            attachments.any { it.uploadState is Attachment.UploadState.Failed } -> {
                                jobsMap[newMessage.id]?.cancel()
                            }
                            else -> Unit
                        }
                    }
            }
            )
        enqueueAttachmentUpload(newMessage, channelType, channelId)
        jobsMap[newMessage.id]?.join()
        return if (allAttachmentsUploaded) {
            logger.d { "[waitForAttachmentsToBeSent] All attachments for message ${newMessage.id} uploaded" }
            Result.Success(messageToBeSent.copy(type = Message.TYPE_REGULAR))
        } else {
            logger.i { "[waitForAttachmentsToBeSent] Could not upload attachments for message ${newMessage.id}" }
            Result.Failure(
                Error.GenericError("Could not upload attachments, not sending message with id ${newMessage.id}"),
            )
        }.also {
            AttachmentsUploadStates.removeMessageAttachmentsState(newMessage.id)
            uploadIds.remove(newMessage.id)
        }
    }

    private fun enqueueAttachmentUpload(message: Message, channelType: String, channelId: String) {
        val workId = UploadAttachmentsAndroidWorker.start(context, channelType, channelId, message.id, networkType)
        uploadIds[message.id] = workId
    }

    fun cancelJobs() {
        AttachmentsUploadStates.clearStates()
        jobsMap.values.forEach { it.cancel() }
        uploadIds.values.forEach { UploadAttachmentsAndroidWorker.stop(context, it) }
    }
}
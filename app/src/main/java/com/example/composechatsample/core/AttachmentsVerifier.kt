package com.example.composechatsample.core

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.log.taggedLogger

internal object AttachmentsVerifier {

    private val logger by taggedLogger("Chat:AttachmentVerifier")

    internal fun verifyAttachments(result: Result<Message>): Result<Message> {
        val message = result.getOrNull() ?: return result
        logger.d { "[verifyAttachments] #uploader; uploadedAttachments: ${message.attachments}" }
        val corruptedAttachment = message.attachments.find {
            it.upload != null && it.imageUrl == null && it.assetUrl == null
        }
        return if (corruptedAttachment == null) {
            result
        } else {
            logger.e {
                "[verifyAttachments] #uploader; message(${message.id}) has corrupted attachment: $corruptedAttachment"
            }
            Result.Failure(
                Error.GenericError("Message(${message.id}) contains corrupted attachment: $corruptedAttachment"),
            )
        }
    }
}
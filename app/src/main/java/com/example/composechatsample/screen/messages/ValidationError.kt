package com.example.composechatsample.screen.messages

import com.example.composechatsample.core.models.Attachment

public sealed class ValidationError {
    public data class MessageLengthExceeded(
        val messageLength: Int,
        val maxMessageLength: Int,
    ) : ValidationError()

    public data class AttachmentSizeExceeded(
        val attachments: List<Attachment>,
        val maxAttachmentSize: Long,
    ) : ValidationError()

    public data class AttachmentCountExceeded(
        val attachmentCount: Int,
        val maxAttachmentCount: Int,
    ) : ValidationError()

    public object ContainsLinksWhenNotAllowed : ValidationError() {
        override fun toString(): String = "ContainsLinksWhenNotAllowed"
    }
}
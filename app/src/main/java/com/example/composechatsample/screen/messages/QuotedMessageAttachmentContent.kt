package com.example.composechatsample.screen.messages

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.screen.MediaGalleryPreviewResult
import com.example.composechatsample.ui.theme.AttachmentState
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun QuotedMessageAttachmentContent(
    message: Message,
    onLongItemClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
) {
    val attachments = message.attachments

    val quoteAttachmentFactory = if (attachments.isNotEmpty()) {
        val quotedFactory = ChatTheme.quotedAttachmentFactories.firstOrNull {
            it.canHandle(message.attachments.take(1))
        }
        quotedFactory ?: ChatTheme.attachmentFactories.firstOrNull { it.canHandle(message.attachments.take(1)) }
    } else {
        null
    }

    val attachmentState = AttachmentState(
        message = message,
        onLongItemClick = onLongItemClick,
        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
    )

    quoteAttachmentFactory?.content?.invoke(
        modifier,
        attachmentState,
    )
}
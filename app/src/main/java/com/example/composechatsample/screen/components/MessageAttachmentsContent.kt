package com.example.composechatsample.screen.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.composechatsample.core.hasLink
import com.example.composechatsample.core.isGiphy
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.screen.MediaGalleryPreviewResult
import com.example.composechatsample.ui.theme.AttachmentState
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun MessageAttachmentsContent(
    message: Message,
    onLongItemClick: (Message) -> Unit,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
) {
    if (message.attachments.isNotEmpty()) {
        val (links, attachments) = message.attachments.partition { it.hasLink() && !it.isGiphy() }

        val linkFactory = if (links.isNotEmpty()) {
            ChatTheme.attachmentFactories.firstOrNull { it.canHandle(links) }
        } else {
            null
        }

        val attachmentFactory = if (attachments.isNotEmpty()) {
            ChatTheme.attachmentFactories.firstOrNull { it.canHandle(attachments) }
        } else {
            null
        }

        val attachmentState = AttachmentState(
            message = message,
            onLongItemClick = onLongItemClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
        )

        if (attachmentFactory != null) {
            attachmentFactory.content(Modifier.padding(2.dp), attachmentState)
        } else if (linkFactory != null) {
            linkFactory.content(Modifier.padding(8.dp), attachmentState)
        }
    }
}
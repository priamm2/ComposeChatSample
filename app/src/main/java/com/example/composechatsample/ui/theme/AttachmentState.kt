package com.example.composechatsample.ui.theme

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.screen.MediaGalleryPreviewResult

public data class AttachmentState(
    val message: Message,
    val onLongItemClick: (Message) -> Unit = {},
    val onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
)
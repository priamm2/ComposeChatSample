package com.example.composechatsample.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.composechatsample.core.models.Attachment

public open class AttachmentFactory constructor(
    public val canHandle: (attachments: List<Attachment>) -> Boolean,
    public val previewContent: (
        @Composable (
        modifier: Modifier,
        attachments: List<Attachment>,
        onAttachmentRemoved: (Attachment) -> Unit,
        ) -> Unit
    )? = null,
    public val content: @Composable (
        modifier: Modifier,
        attachmentState: AttachmentState,
    ) -> Unit,
    public val textFormatter: (attachments: Attachment) -> String = {
        it.title ?: it.name ?: it.fallback ?: ""
    },
)
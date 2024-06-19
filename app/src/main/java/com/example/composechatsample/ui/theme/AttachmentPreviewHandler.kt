package com.example.composechatsample.ui.theme

import android.content.Context
import com.example.composechatsample.common.MediaAttachmentPreviewHandler
import com.example.composechatsample.core.models.Attachment

public interface AttachmentPreviewHandler {

    public fun canHandle(attachment: Attachment): Boolean

    public fun handleAttachmentPreview(attachment: Attachment)

    public companion object {
        public fun defaultAttachmentHandlers(context: Context): List<AttachmentPreviewHandler> {
            return listOf(
                MediaAttachmentPreviewHandler(context),
                DocumentAttachmentPreviewHandler(context),
                UrlAttachmentPreviewHandler(context),
            )
        }
    }
}
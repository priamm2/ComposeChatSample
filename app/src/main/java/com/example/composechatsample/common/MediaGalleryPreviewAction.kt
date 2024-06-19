package com.example.composechatsample.common

import com.example.composechatsample.core.models.Message

internal sealed class MediaGalleryPreviewAction {
    internal abstract val message: Message
}

internal data class Reply(override val message: Message) : MediaGalleryPreviewAction()

internal data class ShowInChat(override val message: Message) : MediaGalleryPreviewAction()

internal data class SaveMedia(override val message: Message) : MediaGalleryPreviewAction()

internal data class Delete(override val message: Message) : MediaGalleryPreviewAction()

package com.example.composechatsample.ui.theme

import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.ui.layout.ContentScale
import com.example.composechatsample.common.StreamCdnImageResizing
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.screen.MediaGalleryPreviewResult

public object StreamAttachmentFactories {

    private const val DEFAULT_LINK_DESCRIPTION_MAX_LINES = 5

    public fun defaultFactories(
        linkDescriptionMaxLines: Int = DEFAULT_LINK_DESCRIPTION_MAX_LINES,
        giphyInfoType: GiphyInfoType = GiphyInfoType.ORIGINAL,
        giphySizingMode: GiphySizingMode = GiphySizingMode.ADAPTIVE,
        contentScale: ContentScale = ContentScale.Crop,
        skipEnrichUrl: Boolean = false,
        onUploadContentItemClick: (
            Attachment,
            List<AttachmentPreviewHandler>,
        ) -> Unit = ::onFileUploadContentItemClick,
        onLinkContentItemClick: (context: Context, previewUrl: String) -> Unit = ::onLinkAttachmentContentClick,
        onGiphyContentItemClick: (context: Context, Url: String) -> Unit = ::onGiphyAttachmentContentClick,
        onMediaContentItemClick: (
            mediaGalleryPreviewLauncher: ManagedActivityResultLauncher<MediaGalleryPreviewContract.Input, MediaGalleryPreviewResult?>,
            message: Message,
            attachmentPosition: Int,
            videoThumbnailsEnabled: Boolean,
            streamCdnImageResizing: StreamCdnImageResizing,
            skipEnrichUrl: Boolean,
        ) -> Unit = ::onMediaAttachmentContentItemClick,
        onFileContentItemClick: (
            previewHandlers: List<AttachmentPreviewHandler>,
            attachment: Attachment,
        ) -> Unit = ::onFileAttachmentContentItemClick,
    ): List<AttachmentFactory> = listOf(
        UploadAttachmentFactory(
            onContentItemClick = onUploadContentItemClick,
        ),
        LinkAttachmentFactory(
            linkDescriptionMaxLines = linkDescriptionMaxLines,
            onContentItemClick = onLinkContentItemClick,
        ),
        GiphyAttachmentFactory(
            giphyInfoType = giphyInfoType,
            giphySizingMode = giphySizingMode,
            contentScale = contentScale,
            onContentItemClick = onGiphyContentItemClick,
        ),
        MediaAttachmentFactory(
            skipEnrichUrl = skipEnrichUrl,
            onContentItemClick = onMediaContentItemClick,
        ),
        FileAttachmentFactory(
            onContentItemClick = onFileContentItemClick,
        ),
        FileAttachmentFactory(),
        AudioRecordAttachmentFactory(),
        UnsupportedAttachmentFactory(),
    )

    public fun defaultQuotedFactories(): List<AttachmentFactory> = listOf(
        QuotedAttachmentFactory(),
    )
}
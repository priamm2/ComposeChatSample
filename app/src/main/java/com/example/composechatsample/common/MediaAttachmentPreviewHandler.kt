package com.example.composechatsample.common

import android.content.Context
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.AttachmentType
import com.example.composechatsample.ui.theme.AttachmentPreviewHandler

public class MediaAttachmentPreviewHandler(private val context: Context) :
    AttachmentPreviewHandler {

    override fun canHandle(attachment: Attachment): Boolean {
        val assetUrl = attachment.assetUrl
        val mimeType = attachment.mimeType ?: ""
        val type = attachment.type ?: ""

        return when {
            assetUrl.isNullOrEmpty() -> false
            mimeType.isBlank() && type.isBlank() -> false
            AttachmentType.AUDIO in mimeType -> true
            AttachmentType.VIDEO in mimeType -> true
            AttachmentType.AUDIO in type -> true
            AttachmentType.VIDEO in type -> true
            buildMimeSubTypeList().any { subtype -> mimeType.contains(subtype) } -> true
            else -> false
        }
    }

    override fun handleAttachmentPreview(attachment: Attachment) {
        context.startActivity(
            MediaPreviewActivity.getIntent(
                context = context,
                url = requireNotNull(attachment.assetUrl),
                title = attachment.title ?: attachment.name,
            ),
        )
    }

    private fun buildMimeSubTypeList() = listOf(
        "mpeg-3", "x-mpeg3", "mp3", "mpeg", "x-mpeg",
        // aac
        "aac",
        // webm
        "webm",
        // wav
        "wav", "x-wav",
        // flac
        "flac", "x-flac",
        // ac3
        "ac3",
        // ogg
        "ogg", "x-ogg",
        // mp4
        "mp4",
        // m4a
        "x-m4a",
        // matroska
        "x-matroska",
        // vorbis
        "vorbis",
        // quicktime
        "quicktime",
    )
}
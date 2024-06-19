package com.example.composechatsample.ui.theme

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.AttachmentType

public class UrlAttachmentPreviewHandler(private val context: Context) : AttachmentPreviewHandler {

    override fun canHandle(attachment: Attachment): Boolean {
        return !getAttachmentUrl(attachment).isNullOrEmpty()
    }

    override fun handleAttachmentPreview(attachment: Attachment) {
        val url = getAttachmentUrl(attachment)
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun getAttachmentUrl(attachment: Attachment): String? {
        with(attachment) {
            return when (type) {
                AttachmentType.IMAGE -> {
                    when {
                        titleLink != null -> titleLink
                        ogUrl != null -> ogUrl
                        assetUrl != null -> assetUrl
                        else -> imageUrl
                    }
                }
                else -> url
            }
        }
    }
}
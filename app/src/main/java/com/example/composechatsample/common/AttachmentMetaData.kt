package com.example.composechatsample.common

import android.content.Context
import android.net.Uri
import com.example.composechatsample.core.StreamFileUtil
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.AttachmentType
import java.io.File

public data class AttachmentMetaData(
    var uri: Uri? = null,
    var type: String? = null,
    var mimeType: String? = null,
    var title: String? = null,
    var file: File? = null,
    var extraData: Map<String, Any> = mapOf(),
) {
    var size: Long = 0
    var isSelected: Boolean = false
    var selectedPosition: Int = 0
    var videoLength: Long = 0

    public constructor(attachment: Attachment) : this(
        type = attachment.type,
        mimeType = attachment.mimeType,
        title = attachment.title,
    )

    public constructor(
        context: Context,
        file: File,
    ) : this(file = file, uri = StreamFileUtil.getUriForFile(context, file)) {
        mimeType = Utils.getMimeType(file)
        type = getTypeFromMimeType(mimeType)
        size = file.length()
        title = file.name
    }

    private fun getTypeFromMimeType(mimeType: String?): String = mimeType?.let { type ->
        when {
            type.contains("image") -> {
                AttachmentType.IMAGE
            }
            type.contains("video") -> {
                AttachmentType.VIDEO
            }
            else -> {
                AttachmentType.FILE
            }
        }
    } ?: AttachmentType.FILE
}
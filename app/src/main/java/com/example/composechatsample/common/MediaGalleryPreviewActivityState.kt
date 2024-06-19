package com.example.composechatsample.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaGalleryPreviewActivityState(
    val messageId: String,
    val userId: String,
    val userName: String,
    val userImage: String,
    val userIsOnline: Boolean = false,
    val attachments: List<MediaGalleryPreviewActivityAttachmentState>,
) : Parcelable
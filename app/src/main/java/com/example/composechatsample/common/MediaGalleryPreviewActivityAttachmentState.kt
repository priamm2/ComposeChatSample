package com.example.composechatsample.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class MediaGalleryPreviewActivityAttachmentState(
    val name: String?,
    val url: String?,
    val thumbUrl: String?,
    val imageUrl: String?,
    val assetUrl: String?,
    val originalWidth: Int?,
    val originalHeight: Int?,
    val type: String?,
) : Parcelable
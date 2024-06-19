package com.example.composechatsample.screen;

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
public class MediaGalleryPreviewResult(
    public val messageId: String,
    public val parentMessageId: String?,
    public val resultType: MediaGalleryPreviewResultType,
) : Parcelable

public enum class MediaGalleryPreviewResultType {
    SHOW_IN_CHAT,
    QUOTE,
}
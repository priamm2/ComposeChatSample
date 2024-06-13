package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable

@Immutable
public data class LinkPreview(
    val originUrl: String,
    val attachment: Attachment,
) {

    public companion object {
        public val EMPTY: LinkPreview = LinkPreview(originUrl = "", attachment = Attachment())
    }
}

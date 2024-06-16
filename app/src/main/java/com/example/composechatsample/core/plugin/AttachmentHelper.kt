package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.TimeProvider
import com.example.composechatsample.core.models.Attachment
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

public class AttachmentHelper(private val timeProvider: TimeProvider = TimeProvider) {

    @Suppress("ReturnCount")
    public fun hasValidImageUrl(attachment: Attachment): Boolean {
        val url = attachment.imageUrl?.toHttpUrlOrNull() ?: return false
        if (url.queryParameterNames.contains(QUERY_KEY_NAME_EXPIRES).not()) {
            return true
        }
        val timestamp = url.queryParameter(QUERY_KEY_NAME_EXPIRES)?.toLongOrNull() ?: return false
        return timestamp > timeProvider.provideCurrentTimeInSeconds()
    }

    public fun hasStreamImageUrl(attachment: Attachment): Boolean {
        return attachment.imageUrl?.toHttpUrlOrNull()?.host?.let(STREAM_CDN_HOST_PATTERN::matches) ?: false
    }

    private companion object {
        private const val QUERY_KEY_NAME_EXPIRES = "Expires"
        private val STREAM_CDN_HOST_PATTERN =
            "stream-chat-+.+\\.imgix.net$|.+\\.stream-io-cdn.com$".toRegex(RegexOption.IGNORE_CASE)
    }
}
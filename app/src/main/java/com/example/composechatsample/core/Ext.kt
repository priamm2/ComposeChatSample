package com.example.composechatsample.core

import android.webkit.MimeTypeMap
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.util.Date
import java.util.UUID

public fun Message.hasPendingAttachments(): Boolean =
    attachments.any {
        it.uploadState is Attachment.UploadState.InProgress ||
                it.uploadState is Attachment.UploadState.Idle
    }

public fun User.mergePartially(that: User): User = this.copy(
    role = that.role,
    createdAt = that.createdAt,
    updatedAt = that.updatedAt,
    lastActive = that.lastActive,
    banned = that.banned,
    name = that.name,
    image = that.image,
    privacySettings = that.privacySettings,
    extraData = that.extraData,
)

@JvmSynthetic
public inline fun <T : Any> Result<T>.stringify(toString: (data: T) -> String): String {
    return when (this) {
        is Result.Success -> toString(value)
        is Result.Failure -> value.toString()
    }
}

internal fun Message.ensureId(currentUser: User?): Message =
    copy(id = id.takeIf { it.isNotBlank() } ?: generateMessageId(currentUser))

private fun generateMessageId(user: User?): String {
    return "${user?.id}-${UUID.randomUUID()}"
}

@Throws(IllegalStateException::class)
public fun String.cidToTypeAndId(): Pair<String, String> {
    check(isNotEmpty()) { "cid can not be empty" }
    check(':' in this) { "cid needs to be in the format channelType:channelId. For example, messaging:123" }
    return checkNotNull(split(":").takeIf { it.size >= 2 }?.let { it.first() to it.last() })
}

internal fun Date.isLaterThanDays(daysInMillis: Long): Boolean {
    val now = Date()
    return now.time - time > daysInMillis
}

internal fun File.getMimeType(): String =
    MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"

internal fun File.getMediaType(): MediaType = getMimeType().toMediaType()


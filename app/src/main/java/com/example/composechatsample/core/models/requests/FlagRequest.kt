package com.example.composechatsample.core.models.requests

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

internal sealed class FlagRequest {
    abstract val reason: String?
    abstract val custom: Map<String, String>
}

@JsonClass(generateAdapter = true)
internal data class FlagMessageRequest(
    @Json(name = "target_message_id") val targetMessageId: String,
    @Json(name = "reason") override val reason: String?,
    @Json(name = "custom") override val custom: Map<String, String>,
) : FlagRequest()

@JsonClass(generateAdapter = true)
internal data class FlagUserRequest(
    @Json(name = "target_user_id") val targetUserId: String,
    @Json(name = "reason") override val reason: String?,
    @Json(name = "custom") override val custom: Map<String, String>,
) : FlagRequest()
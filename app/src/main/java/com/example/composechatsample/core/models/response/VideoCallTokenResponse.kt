package com.example.composechatsample.core.models.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoCallTokenResponse(
    val token: String,
    @field:Json(name = "agora_uid") val agoraUid: Int?,
    @field:Json(name = "agora_app_id") val agoraAppId: String?,
)
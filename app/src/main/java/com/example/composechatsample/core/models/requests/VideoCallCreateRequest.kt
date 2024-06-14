package com.example.composechatsample.core.models.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class VideoCallCreateRequest(
    val id: String,
    val type: String,
)
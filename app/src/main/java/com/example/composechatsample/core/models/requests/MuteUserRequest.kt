package com.example.composechatsample.core.models.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class MuteUserRequest(
    val target_id: String,
    val user_id: String,
    val timeout: Int?,
)

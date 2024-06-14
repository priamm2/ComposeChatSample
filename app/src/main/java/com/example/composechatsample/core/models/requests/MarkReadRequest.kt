package com.example.composechatsample.core.models.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class MarkReadRequest(
    val message_id: String,
)
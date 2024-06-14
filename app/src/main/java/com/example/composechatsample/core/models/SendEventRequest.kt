package com.example.composechatsample.core.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SendEventRequest(
    val event: Map<Any, Any>,
)
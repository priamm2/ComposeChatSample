package com.example.composechatsample.core.models.requests

import com.example.composechatsample.core.models.dto.UpstreamMessageDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UpdateMessageRequest(
    val message: UpstreamMessageDto,
    val skip_enrich_url: Boolean = false,
)
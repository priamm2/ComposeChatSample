package com.example.composechatsample.core.models.requests

import com.example.composechatsample.core.models.dto.UpstreamMessageDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UpdateChannelRequest(
    val data: Map<String, Any>,
    val message: UpstreamMessageDto?,
)
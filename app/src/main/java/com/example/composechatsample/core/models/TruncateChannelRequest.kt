package com.example.composechatsample.core.models

import com.example.composechatsample.core.models.dto.UpstreamMessageDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class TruncateChannelRequest(
    val message: UpstreamMessageDto?,
)
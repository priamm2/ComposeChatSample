package com.example.composechatsample.core.models.response

import com.example.composechatsample.core.models.dto.DownstreamMessageDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessageResponse(
    val message: DownstreamMessageDto,
)
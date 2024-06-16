package com.example.composechatsample.core.models.response

import com.example.composechatsample.core.models.dto.DownstreamReactionDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ReactionResponse(
    val reaction: DownstreamReactionDto,
)
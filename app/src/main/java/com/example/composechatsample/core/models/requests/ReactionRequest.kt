package com.example.composechatsample.core.models.requests

import com.example.composechatsample.core.models.dto.UpstreamReactionDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ReactionRequest(
    val reaction: UpstreamReactionDto,
    val enforce_unique: Boolean,
)
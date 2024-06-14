package com.example.composechatsample.core.models.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ReactionResponse(
    val reaction: DownstreamReactionDto,
)
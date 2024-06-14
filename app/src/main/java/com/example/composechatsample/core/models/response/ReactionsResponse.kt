package com.example.composechatsample.core.models.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ReactionsResponse(
    val reactions: List<DownstreamReactionDto>,
)
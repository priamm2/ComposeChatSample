package com.example.composechatsample.core.models.response

import com.example.composechatsample.core.models.dto.DownstreamFlagDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class FlagResponse(
    val flag: DownstreamFlagDto,
)
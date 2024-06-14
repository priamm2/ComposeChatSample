package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class DownstreamReactionGroupDto(
    val count: Int,
    val sum_scores: Int,
    val first_reaction_at: Date,
    val last_reaction_at: Date,
)
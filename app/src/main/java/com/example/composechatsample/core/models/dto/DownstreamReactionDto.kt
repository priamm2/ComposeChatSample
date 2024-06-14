package com.example.composechatsample.core.models.dto

import com.example.composechatsample.core.StreamHandsOff
import com.squareup.moshi.JsonClass
import java.util.Date

@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
            "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
data class DownstreamReactionDto(
    val created_at: Date?,
    val message_id: String,
    val score: Int,
    val type: String,
    val updated_at: Date?,
    val user: DownstreamUserDto?,
    val user_id: String,

    val extraData: Map<String, Any>,
) : ExtraDataDto
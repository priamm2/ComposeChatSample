package com.example.composechatsample.core.models.dto

import com.example.composechatsample.core.StreamHandsOff
import com.squareup.moshi.JsonClass

@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
data class DownstreamModerationDetailsDto(
    val original_text: String?,
    val action: String?,
    val error_msg: String? = null,
    val extraData: Map<String, Any>,
)
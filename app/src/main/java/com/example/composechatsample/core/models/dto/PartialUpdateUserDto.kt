package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class PartialUpdateUserDto(
    val id: String,
    val set: Map<String, Any>,
    val unset: List<String>,
)
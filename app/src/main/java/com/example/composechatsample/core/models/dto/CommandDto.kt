package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommandDto(
    val name: String,
    val description: String,
    val args: String,
    val set: String,
)
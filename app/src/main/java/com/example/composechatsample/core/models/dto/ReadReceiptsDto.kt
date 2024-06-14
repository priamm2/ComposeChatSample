package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReadReceiptsDto(
    val enabled: Boolean,
)
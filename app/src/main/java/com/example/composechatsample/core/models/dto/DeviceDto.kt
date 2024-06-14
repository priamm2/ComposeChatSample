package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviceDto(
    val id: String,
    val push_provider: String,
    val provider_name: String?,
)
package com.example.composechatsample.core.models.response

import com.example.composechatsample.core.models.dto.DeviceDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class DevicesResponse(
    val devices: List<DeviceDto>,
)
package com.example.composechatsample.core.models.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HMSDto(
    @field:Json(name = "room_id") val roomId: String,
    @field:Json(name = "room_name") val roomName: String,
) : VideoCallDto
package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoCallInfoDto(
    val id: String,
    val provider: String,
    val type: String,
    val agora: AgoraDto,
    val hms: HMSDto,
)
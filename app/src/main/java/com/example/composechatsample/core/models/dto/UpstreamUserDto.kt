package com.example.composechatsample.core.models.dto

import com.example.composechatsample.core.StreamHandsOff
import com.squareup.moshi.JsonClass

@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
data class UpstreamUserDto(
    val banned: Boolean,
    val id: String,
    val name: String,
    val image: String,
    val invisible: Boolean,
    val privacy_settings: PrivacySettingsDto?,
    val language: String,
    val role: String,
    val devices: List<DeviceDto>,
    val teams: List<String>,

    val extraData: Map<String, Any>,
) : ExtraDataDto
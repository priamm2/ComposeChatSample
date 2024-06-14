package com.example.composechatsample.core.models.dto

import com.example.composechatsample.core.StreamHandsOff
import com.squareup.moshi.JsonClass
import java.util.Date

@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
data class DownstreamUserDto(
    val id: String,
    val name: String?,
    val image: String?,
    val role: String,
    val invisible: Boolean? = false,
    val privacy_settings: PrivacySettingsDto?,
    val language: String?,
    val banned: Boolean,
    val devices: List<DeviceDto>?,
    val online: Boolean,
    val created_at: Date?,
    val deactivated_at: Date?,
    val updated_at: Date?,
    val last_active: Date?,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
    val unread_count: Int = 0,
    val mutes: List<DownstreamMuteDto>?,
    val teams: List<String> = emptyList(),
    val channel_mutes: List<DownstreamChannelMuteDto>?,

    val extraData: Map<String, Any>,
) : ExtraDataDto
package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class DownstreamChannelMuteDto(
    val user: DownstreamUserDto,
    val channel: DownstreamChannelDto,
    val created_at: Date,
    val updated_at: Date,
    val expires: Date?,
)
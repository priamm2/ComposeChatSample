package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class DownstreamMuteDto(
    val user: DownstreamUserDto,
    val target: DownstreamUserDto,
    val created_at: Date,
    val updated_at: Date,
    val expires: Date?,
)
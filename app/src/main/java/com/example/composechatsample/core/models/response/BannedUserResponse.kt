package com.example.composechatsample.core.models.response

import com.example.composechatsample.core.models.dto.DownstreamChannelDto
import com.example.composechatsample.core.models.dto.DownstreamUserDto
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class BannedUserResponse(
    val user: DownstreamUserDto,
    val banned_by: DownstreamUserDto?,
    val channel: DownstreamChannelDto?,
    val created_at: Date?,
    val expires: Date?,
    val shadow: Boolean = false,
    val reason: String?,
)
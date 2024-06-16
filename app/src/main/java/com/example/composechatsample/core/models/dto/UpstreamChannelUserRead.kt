package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class UpstreamChannelUserRead(
    val user: UpstreamUserDto,
    val last_read: Date,
    val unread_messages: Int,
)
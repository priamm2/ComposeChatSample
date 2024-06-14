package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class DownstreamChannelUserRead(
    val user: DownstreamUserDto,
    val last_read: Date,
    val unread_messages: Int,
    val last_read_message_id: String?,
)
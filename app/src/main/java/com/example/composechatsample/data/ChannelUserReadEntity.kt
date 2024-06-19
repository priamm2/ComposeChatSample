package com.example.composechatsample.data

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class ChannelUserReadEntity(
    val userId: String,
    val lastReceivedEventDate: Date,
    val unreadMessages: Int,
    val lastRead: Date,
    val lastReadMessageId: String?,
)
package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable
import com.example.composechatsample.core.models.UserEntity
import java.util.Date

@Immutable
public data class ChannelUserRead(
    override val user: User,
    val lastReceivedEventDate: Date,
    val unreadMessages: Int,
    val lastRead: Date,
    val lastReadMessageId: String?,
) : UserEntity

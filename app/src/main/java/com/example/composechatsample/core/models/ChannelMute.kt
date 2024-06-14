package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable
import java.util.Date

@Immutable
data class ChannelMute(
    val user: User,
    val channel: Channel,
    val createdAt: Date,
    val updatedAt: Date,
    val expires: Date?,
)

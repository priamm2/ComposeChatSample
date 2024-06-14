package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class DownstreamMemberDto(
    val user: DownstreamUserDto,
    val created_at: Date?,
    val updated_at: Date?,
    val invited: Boolean?,
    val invite_accepted_at: Date?,
    val invite_rejected_at: Date?,
    val shadow_banned: Boolean = false,
    val banned: Boolean = false,
    val channel_role: String?,
    val notifications_muted: Boolean?,
    val status: String?,
)
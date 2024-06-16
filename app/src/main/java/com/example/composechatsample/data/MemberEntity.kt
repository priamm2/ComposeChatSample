package com.example.composechatsample.data

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class MemberEntity(
    var userId: String,
    var role: String = "",
    var createdAt: Date? = null,
    var updatedAt: Date? = null,
    var isInvited: Boolean = false,
    var inviteAcceptedAt: Date? = null,
    var inviteRejectedAt: Date? = null,
    var shadowBanned: Boolean = false,
    var banned: Boolean = false,
    val channelRole: String? = null,
    val notificationsMuted: Boolean? = null,
    val status: String? = null,
)
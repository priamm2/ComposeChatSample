package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable
import com.example.composechatsample.core.models.UserEntity
import com.example.composechatsample.core.models.querysort.ComparableFieldProvider
import java.util.Date

@Immutable
public data class Member(
    override val user: User,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val isInvited: Boolean? = null,
    val inviteAcceptedAt: Date? = null,
    val inviteRejectedAt: Date? = null,
    val shadowBanned: Boolean = false,
    val banned: Boolean = false,
    val channelRole: String? = null,
    val notificationsMuted: Boolean? = null,
    val status: String? = null,
) : UserEntity, ComparableFieldProvider {

    override fun getComparableField(fieldName: String): Comparable<*>? {
        return when (fieldName) {
            "user_id", "userId" -> getUserId()
            "created_at", "createdAt" -> createdAt
            "updated_at", "updatedAt" -> updatedAt
            "is_invited", "isInvited" -> isInvited
            "invite_accepted_at", "inviteAcceptedAt" -> inviteAcceptedAt
            "invite_rejected_at", "inviteRejectedAt" -> inviteRejectedAt
            "shadow_banned", "shadowBanned" -> shadowBanned
            "banned" -> banned
            "channel_role", "channelRole" -> channelRole
            "notifications_muted", "notificationsMuted" -> notificationsMuted
            "status" -> status
            else -> null
        }
    }
}

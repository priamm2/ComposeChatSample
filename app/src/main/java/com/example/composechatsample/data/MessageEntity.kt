package com.example.composechatsample.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.composechatsample.core.models.SyncStatus
import java.util.Date

data class MessageEntity(
    @Embedded val messageInnerEntity: MessageInnerEntity,
    @Relation(entity = AttachmentEntity::class, parentColumn = "id", entityColumn = "messageId")
    val attachments: List<AttachmentEntity>,
    @Relation(entity = ReactionEntity::class, parentColumn = "id", entityColumn = "messageId")
    val ownReactions: List<ReactionEntity> = emptyList(),
    @Relation(entity = ReactionEntity::class, parentColumn = "id", entityColumn = "messageId")
    val latestReactions: List<ReactionEntity> = emptyList(),
)

@Entity(
    tableName = MESSAGE_ENTITY_TABLE_NAME,
    indices = [
        Index(value = ["cid", "createdAt"]),
        Index(value = ["syncStatus"]),
    ],
)
data class MessageInnerEntity(
    @PrimaryKey
    val id: String,
    val cid: String,
    val userId: String,
    val text: String = "",
    val html: String = "",
    val type: String = "",
    val syncStatus: SyncStatus = SyncStatus.COMPLETED,
    val replyCount: Int = 0,
    val deletedReplyCount: Int = 0,
    val createdAt: Date? = null,
    val createdLocallyAt: Date? = null,
    val updatedAt: Date? = null,
    val updatedLocallyAt: Date? = null,
    val deletedAt: Date? = null,
    val remoteMentionedUserIds: List<String> = emptyList(),
    val mentionedUsersId: List<String> = emptyList(),
    val reactionCounts: Map<String, Int> = emptyMap(),
    val reactionScores: Map<String, Int> = emptyMap(),
    val reactionGroups: Map<String, ReactionGroupEntity> = emptyMap(),
    val parentId: String? = null,
    val command: String? = null,
    val shadowed: Boolean = false,
    val i18n: Map<String, String> = emptyMap(),
    val showInChannel: Boolean = false,
    @Embedded(prefix = "channel_info")
    val channelInfo: ChannelInfoEntity? = null,
    val silent: Boolean = false,
    val extraData: Map<String, Any> = emptyMap(),
    val replyToId: String?,
    val pinned: Boolean,
    val pinnedAt: Date? = null,
    val pinExpires: Date? = null,
    val pinnedByUserId: String?,
    val threadParticipantsIds: List<String> = emptyList(),
    var skipPushNotification: Boolean = false,
    var skipEnrichUrl: Boolean = false,
    val moderationDetails: ModerationDetailsEntity? = null,
    val messageTextUpdatedAt: Date? = null,
)

internal const val MESSAGE_ENTITY_TABLE_NAME = "stream_chat_message"
package com.example.composechatsample.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.composechatsample.core.models.SyncStatus
import java.util.Date

@Entity(tableName = CHANNEL_ENTITY_TABLE_NAME, indices = [Index(value = ["syncStatus"])])
internal data class ChannelEntity(
    val type: String,
    val channelId: String,
    val name: String,
    val image: String,
    val cooldown: Int,
    val createdByUserId: String,
    val frozen: Boolean,
    val hidden: Boolean?,
    val hideMessagesBefore: Date?,
    val members: Map<String, MemberEntity>,
    val memberCount: Int,
    val watcherIds: List<String>,
    val watcherCount: Int,
    val reads: Map<String, ChannelUserReadEntity>,
    val lastMessageAt: Date?,
    val lastMessageId: String?,
    val createdAt: Date?,
    val updatedAt: Date?,
    val deletedAt: Date?,
    val extraData: Map<String, Any>,
    val syncStatus: SyncStatus,
    val team: String,
    val ownCapabilities: Set<String>,
    val membership: MemberEntity?,
) {

    @PrimaryKey
    var cid: String = "%s:%s".format(type, channelId)
}

internal const val CHANNEL_ENTITY_TABLE_NAME = "stream_chat_channel_state"
package com.example.composechatsample.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.composechatsample.core.models.SyncStatus
import java.util.Date

@Entity(
    tableName = REACTION_ENTITY_TABLE_NAME,
    indices = [
        Index(
            value = ["messageId", "userId", "type"],
            unique = true,
        ), Index(value = ["syncStatus"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = MessageInnerEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE,
            deferred = true,
        ),
    ],
)
internal data class ReactionEntity(
    @ColumnInfo(index = true)
    val messageId: String,
    val userId: String,
    val type: String,
    val score: Int = 1,
    val createdAt: Date? = null,
    val createdLocallyAt: Date? = null,
    val updatedAt: Date? = null,
    val deletedAt: Date? = null,
    val enforceUnique: Boolean = false,
    val extraData: Map<String, Any>,
    val syncStatus: SyncStatus,
) {
    @PrimaryKey
    var id = messageId.hashCode() + userId.hashCode() + type.hashCode()
}

internal const val REACTION_ENTITY_TABLE_NAME = "stream_chat_reaction"
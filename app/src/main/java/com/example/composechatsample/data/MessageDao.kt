package com.example.composechatsample.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.composechatsample.core.models.SyncStatus
import java.util.Date

@Dao
@Suppress("TooManyFunctions")
internal interface MessageDao {

    @Transaction
    suspend fun insert(messageEntities: List<MessageEntity>) {
        upsertMessageInnerEntities(messageEntities.map(MessageEntity::messageInnerEntity))
        deleteAttachments(messageEntities.map { it.messageInnerEntity.id })
        insertAttachments(messageEntities.flatMap(MessageEntity::attachments))
        insertReactions(messageEntities.flatMap { it.latestReactions + it.ownReactions })
    }

    @Transaction
    fun deleteAttachments(messageIds: List<String>) {
        messageIds.chunked(SQLITE_MAX_VARIABLE_NUMBER).forEach(::deleteAttachmentsChunked)
    }

    @Query("DELETE FROM attachment_inner_entity WHERE messageId in (:messageIds)")
    fun deleteAttachmentsChunked(messageIds: List<String>)

    @Transaction
    suspend fun insert(messageEntity: MessageEntity) = insert(listOf(messageEntity))

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessageInnerEntity(messageInnerEntity: MessageInnerEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessageInnerEntities(messageInnerEntities: List<MessageInnerEntity>): List<Long>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateMessageInnerEntity(messageInnerEntity: MessageInnerEntity)

    @Transaction
    suspend fun upsertMessageInnerEntity(messageInnerEntity: MessageInnerEntity) {
        val rowId = insertMessageInnerEntity(messageInnerEntity)
        if (rowId == -1L) {
            updateMessageInnerEntity(messageInnerEntity)
        }
    }

    @Transaction
    suspend fun upsertMessageInnerEntities(messageInnerEntities: List<MessageInnerEntity>) {
        val rowIds = insertMessageInnerEntities(messageInnerEntities)
        val entitiesToUpdate = rowIds.mapIndexedNotNull { index, rowId ->
            if (rowId == -1L) messageInnerEntities[index] else null
        }
        entitiesToUpdate.forEach { updateMessageInnerEntity(it) }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachmentEntities: List<AttachmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReactions(reactions: List<ReactionEntity>)

    @Query(
        "SELECT * from $MESSAGE_ENTITY_TABLE_NAME " +
            "WHERE cid = :cid " +
            "AND (createdAt > :dateFilter OR createdLocallyAt > :dateFilter) " +
            "ORDER BY CASE WHEN createdAt " +
            "IS NULL THEN createdLocallyAt " +
            "ELSE createdAt " +
            "END ASC LIMIT :limit",
    )
    @Transaction
    suspend fun messagesForChannelNewerThan(
        cid: String,
        limit: Int = 100,
        dateFilter: Date,
    ): List<MessageEntity>

    @Query(
        "SELECT * from $MESSAGE_ENTITY_TABLE_NAME " +
            "WHERE cid = :cid " +
            "AND (createdAt >= :dateFilter OR createdLocallyAt >= :dateFilter) " +
            "ORDER BY CASE WHEN createdAt " +
            "IS NULL THEN createdLocallyAt " +
            "ELSE createdAt " +
            "END ASC LIMIT :limit",
    )
    @Transaction
    suspend fun messagesForChannelEqualOrNewerThan(
        cid: String,
        limit: Int = 100,
        dateFilter: Date,
    ): List<MessageEntity>

    @Query(
        "SELECT * from $MESSAGE_ENTITY_TABLE_NAME " +
            "WHERE cid = :cid " +
            "AND (createdAt < :dateFilter OR createdLocallyAt < :dateFilter) " +
            "ORDER BY CASE WHEN createdAt " +
            "IS NULL THEN createdLocallyAt " +
            "ELSE createdAt " +
            "END DESC LIMIT :limit",
    )
    @Transaction
    suspend fun messagesForChannelOlderThan(
        cid: String,
        limit: Int = 100,
        dateFilter: Date,
    ): List<MessageEntity>

    @Query(
        "SELECT * from $MESSAGE_ENTITY_TABLE_NAME " +
            "WHERE cid = :cid " +
            "AND (createdAt <= :dateFilter OR createdLocallyAt <= :dateFilter) " +
            "ORDER BY CASE WHEN createdAt " +
            "IS NULL THEN createdLocallyAt " +
            "ELSE createdAt " +
            "END DESC LIMIT :limit",
    )
    @Transaction
    suspend fun messagesForChannelEqualOrOlderThan(
        cid: String,
        limit: Int = 100,
        dateFilter: Date,
    ): List<MessageEntity>

    @Query(
        "SELECT * from $MESSAGE_ENTITY_TABLE_NAME " +
            "WHERE cid = :cid " +
            "ORDER BY CASE WHEN createdAt " +
            "IS NULL THEN createdLocallyAt " +
            "ELSE createdAt " +
            "END DESC LIMIT :limit",
    )
    @Transaction
    suspend fun messagesForChannel(cid: String, limit: Int = 100): List<MessageEntity>

    @Query(
        "SELECT * from $MESSAGE_ENTITY_TABLE_NAME " +
            "WHERE parentId = :messageId OR id = :messageId " +
            "ORDER BY CASE WHEN createdAt " +
            "IS NULL THEN createdLocallyAt " +
            "ELSE createdAt " +
            "END DESC LIMIT :limit",
    )
    @Transaction
    suspend fun messagesForThread(messageId: String, limit: Int = 100): List<MessageEntity>

    @Query(
        "DELETE from $MESSAGE_ENTITY_TABLE_NAME " +
            "WHERE cid = :cid " +
            "AND createdAt < :deleteMessagesBefore",
    )
    suspend fun deleteChannelMessagesBefore(cid: String, deleteMessagesBefore: Date)

    @Query(
        "DELETE from $MESSAGE_ENTITY_TABLE_NAME " +
            "WHERE cid = :cid " +
            "AND id = :messageId",
    )
    suspend fun deleteMessage(cid: String, messageId: String)

    @Query(
        "DELETE from $MESSAGE_ENTITY_TABLE_NAME " +
            "WHERE cid = :cid",
    )
    suspend fun deleteMessages(cid: String)

    @Transaction
    suspend fun select(ids: List<String>): List<MessageEntity> {
        return ids.chunked(SQLITE_MAX_VARIABLE_NUMBER).flatMap { messageIds -> selectChunked(messageIds) }
    }

    @Query("SELECT * FROM $MESSAGE_ENTITY_TABLE_NAME WHERE id IN (:ids)")
    @Transaction
    suspend fun selectChunked(ids: List<String>): List<MessageEntity>

    @Query("SELECT * FROM $MESSAGE_ENTITY_TABLE_NAME WHERE id IN (:id)")
    @Transaction
    suspend fun select(id: String): MessageEntity?

    @Transaction
    suspend fun selectWaitForAttachments(): List<MessageEntity> {
        return selectBySyncStatus(SyncStatus.AWAITING_ATTACHMENTS)
    }

    @Query(
        "SELECT * FROM $MESSAGE_ENTITY_TABLE_NAME " +
            "WHERE syncStatus = :syncStatus " +
            "ORDER BY CASE WHEN createdAt IS NULL THEN createdLocallyAt ELSE createdAt END ASC " +
            "LIMIT :limit",
    )
    @Transaction
    suspend fun selectBySyncStatus(syncStatus: SyncStatus, limit: Int = NO_LIMIT): List<MessageEntity>

    @Query(
        "SELECT id FROM $MESSAGE_ENTITY_TABLE_NAME " +
            "WHERE syncStatus = :syncStatus " +
            "ORDER BY CASE WHEN createdAt IS NULL THEN createdLocallyAt ELSE createdAt END ASC " +
            "LIMIT :limit",
    )
    suspend fun selectIdsBySyncStatus(syncStatus: SyncStatus, limit: Int = NO_LIMIT): List<String>

    @Query("DELETE FROM $MESSAGE_ENTITY_TABLE_NAME")
    suspend fun deleteAll()

    private companion object {
        private const val SQLITE_MAX_VARIABLE_NUMBER: Int = 999
        private const val NO_LIMIT: Int = -1
    }
}
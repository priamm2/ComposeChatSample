package com.example.composechatsample.data

import androidx.room.Dao
import androidx.room.Query
import com.example.composechatsample.data.AttachmentEntity.Companion.ATTACHMENT_ENTITY_TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
internal interface AttachmentDao {
    @Query("SELECT * FROM attachment_inner_entity WHERE messageId == :messageId")
    fun observeAttachmentsForMessage(messageId: String): Flow<List<AttachmentEntity>>

    @Query("DELETE FROM $ATTACHMENT_ENTITY_TABLE_NAME")
    fun deleteAll()
}
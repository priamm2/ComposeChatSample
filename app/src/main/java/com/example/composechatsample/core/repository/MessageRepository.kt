package com.example.composechatsample.core.repository

import com.example.composechatsample.core.api.AnyChannelPaginationRequest
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.SyncStatus
import java.util.Date

public interface MessageRepository {

    public suspend fun selectMessagesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest?,
    ): List<Message>

    public suspend fun selectMessagesForThread(
        messageId: String,
        limit: Int,
    ): List<Message>

    public suspend fun selectMessages(messageIds: List<String>): List<Message>

    public suspend fun selectMessage(messageId: String): Message?

    public suspend fun insertMessages(messages: List<Message>)

    public suspend fun insertMessage(message: Message)

    public suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date)

    public suspend fun deleteChannelMessages(cid: String)

    public suspend fun deleteChannelMessage(message: Message)

    public suspend fun selectMessageIdsBySyncState(syncStatus: SyncStatus): List<String>

    public suspend fun selectMessageBySyncState(syncStatus: SyncStatus): List<Message>

    public suspend fun clear()
}
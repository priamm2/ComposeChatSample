package com.example.composechatsample.core.repository

import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import java.util.Date

public interface ChannelRepository {
    public suspend fun insertChannel(channel: Channel)
    public suspend fun insertChannels(channels: Collection<Channel>)

    public suspend fun deleteChannel(cid: String)

    public suspend fun deleteChannelMessage(message: Message)

    public suspend fun selectAllCids(): List<String>

    public suspend fun selectChannels(channelCIDs: List<String>): List<Channel>

    public suspend fun selectChannel(cid: String): Channel?

    public suspend fun selectChannelCidsBySyncNeeded(limit: Int = NO_LIMIT): List<String>

    public suspend fun selectChannelsSyncNeeded(limit: Int = NO_LIMIT): List<Channel>

    public suspend fun setChannelDeletedAt(cid: String, deletedAt: Date)

    public suspend fun setHiddenForChannel(cid: String, hidden: Boolean, hideMessagesBefore: Date)

    public suspend fun setHiddenForChannel(cid: String, hidden: Boolean)

    public suspend fun selectMembersForChannel(cid: String): List<Member>

    public suspend fun updateMembersForChannel(cid: String, members: List<Member>)

    public suspend fun updateLastMessageForChannel(cid: String, lastMessage: Message)

    public suspend fun evictChannel(cid: String)

    public suspend fun clear()

    private companion object {
        private const val NO_LIMIT: Int = -1
    }
}
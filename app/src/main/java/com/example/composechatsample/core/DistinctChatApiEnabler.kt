package com.example.composechatsample.core

import com.example.composechatsample.core.api.QueryChannelRequest
import com.example.composechatsample.core.api.QueryChannelsRequest
import com.example.composechatsample.core.models.BannedUser
import com.example.composechatsample.core.models.BannedUsersSort
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.querysort.QuerySorter
import java.util.Date

internal class DistinctChatApiEnabler(
    private val distinctApi: DistinctChatApi,
    private val distinctCallsEnabled: () -> Boolean,
) : ChatApi by distinctApi.delegate {

    private val originalApi = distinctApi.delegate

    override fun getRepliesMore(messageId: String, firstId: String, limit: Int): Call<List<Message>> {
        return getApi().getRepliesMore(messageId, firstId, limit)
    }

    override fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        return getApi().getReplies(messageId, limit)
    }

    override fun getNewerReplies(parentId: String, limit: Int, lastId: String?): Call<List<Message>> {
        return getApi().getNewerReplies(parentId, limit, lastId)
    }

    override fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>> {
        return getApi().getReactions(messageId, offset, limit)
    }

    override fun getMessage(messageId: String): Call<Message> {
        return getApi().getMessage(messageId)
    }

    override fun getPinnedMessages(
        channelType: String,
        channelId: String,
        limit: Int,
        sort: QuerySorter<Message>,
        pagination: PinnedMessagesPagination,
    ): Call<List<Message>> {
        return getApi().getPinnedMessages(channelType, channelId, limit, sort, pagination)
    }

    override fun queryChannels(query: QueryChannelsRequest): Call<List<Channel>> {
        return getApi().queryChannels(query)
    }

    override fun queryBannedUsers(
        filter: FilterObject,
        sort: QuerySorter<BannedUsersSort>,
        offset: Int?,
        limit: Int?,
        createdAtAfter: Date?,
        createdAtAfterOrEqual: Date?,
        createdAtBefore: Date?,
        createdAtBeforeOrEqual: Date?,
    ): Call<List<BannedUser>> {
        return getApi().queryBannedUsers(
            filter,
            sort,
            offset,
            limit,
            createdAtAfter,
            createdAtAfterOrEqual,
            createdAtBefore,
            createdAtBeforeOrEqual,
        )
    }

    override fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ): Call<List<Member>> {
        return getApi().queryMembers(channelType, channelId, offset, limit, filter, sort, members)
    }

    override fun queryChannel(channelType: String, channelId: String, query: QueryChannelRequest): Call<Channel> {
        return getApi().queryChannel(channelType, channelId, query)
    }

    private fun getApi() = if (distinctCallsEnabled()) distinctApi else originalApi
}
package com.example.composechatsample.core

import com.example.composechatsample.core.api.QueryChannelsRequest
import com.example.composechatsample.core.models.BannedUser
import com.example.composechatsample.core.models.BannedUsersSort
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.ChannelQueryKey
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.GetNewerRepliesHash
import com.example.composechatsample.core.models.GetPinnedMessagesHash
import com.example.composechatsample.core.models.GetReactionsHash
import com.example.composechatsample.core.models.GetRepliesHash
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.QueryBanedUsersHash
import com.example.composechatsample.core.models.QueryMembersHash
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.querysort.QuerySorter
import com.example.composechatsample.log.StreamLog
import kotlinx.coroutines.CoroutineScope
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

internal class DistinctChatApi(
    private val scope: CoroutineScope,
    internal val delegate: ChatApi,
) : ChatApi by delegate {

    private val distinctCalls = ConcurrentHashMap<Int, DistinctCall<out Any>>()

    override fun queryChannel(channelType: String, channelId: String, query: com.example.composechatsample.core.api.QueryChannelRequest): Call<Channel> {
        val uniqueKey = ChannelQueryKey.from(channelType, channelId, query).hashCode()
        StreamLog.d(TAG) { "[queryChannel] channelType: $channelType, channelId: $channelId, uniqueKey: $uniqueKey" }
        return getOrCreate(uniqueKey) {
            delegate.queryChannel(channelType, channelId, query)
        }
    }

    override fun getRepliesMore(messageId: String, firstId: String, limit: Int): Call<List<Message>> {
        val uniqueKey = GetRepliesHash(messageId, firstId, limit).hashCode()
        StreamLog.d(TAG) {
            "[getRepliesMore] messageId: $messageId, firstId: $firstId, limit: $limit, uniqueKey: $uniqueKey"
        }
        return getOrCreate(uniqueKey) {
            delegate.getRepliesMore(messageId, firstId, limit)
        }
    }

    override fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        val uniqueKey = GetRepliesHash(messageId, null, limit).hashCode()
        StreamLog.d(TAG) { "[getReplies] messageId: $messageId, limit: $limit, uniqueKey: $uniqueKey" }
        return getOrCreate(uniqueKey) {
            delegate.getReplies(messageId, limit)
        }
    }

    override fun getNewerReplies(parentId: String, limit: Int, lastId: String?): Call<List<Message>> {
        val uniqueKey = GetNewerRepliesHash(parentId, limit, lastId).hashCode()
        StreamLog.d(TAG) {
            "[getNewerReplies] parentId: $parentId, limit: $limit, lastId: $lastId, uniqueKey: $uniqueKey"
        }
        return getOrCreate(uniqueKey) {
            delegate.getNewerReplies(parentId, limit, lastId)
        }
    }

    override fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>> {
        val uniqueKey = GetReactionsHash(messageId, offset, limit).hashCode()
        StreamLog.d(TAG) {
            "[getReactions] messageId: $messageId, offset: $offset, limit: $limit, uniqueKey: $uniqueKey"
        }
        return getOrCreate(uniqueKey) {
            delegate.getReactions(messageId, offset, limit)
        }
    }

    override fun getMessage(messageId: String): Call<Message> {
        val uniqueKey = messageId.hashCode()
        StreamLog.d(TAG) { "[getMessage] messageId: $messageId, uniqueKey: $uniqueKey" }
        return getOrCreate(uniqueKey) {
            delegate.getMessage(messageId)
        }
    }

    override fun getPinnedMessages(
        channelType: String,
        channelId: String,
        limit: Int,
        sort: QuerySorter<Message>,
        pagination: PinnedMessagesPagination,
    ): Call<List<Message>> {
        val uniqueKey = GetPinnedMessagesHash(channelType, channelId, limit, sort, pagination).hashCode()
        StreamLog.d(TAG) {
            "[getPinnedMessages] channelType: $channelType, channelId: $channelId, " +
                "limit: $limit, sort: $sort, pagination: $pagination, uniqueKey: $uniqueKey"
        }
        return getOrCreate(uniqueKey) {
            delegate.getPinnedMessages(channelType, channelId, limit, sort, pagination)
        }
    }

    override fun queryChannels(query: QueryChannelsRequest): Call<List<Channel>> {
        val uniqueKey = query.hashCode()
        StreamLog.d(TAG) { "[queryChannels] query: $query, uniqueKey: $uniqueKey" }
        return getOrCreate(uniqueKey) {
            delegate.queryChannels(query)
        }
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
        val uniqueKey = QueryBanedUsersHash(
            filter,
            sort,
            offset,
            limit,
            createdAtAfter,
            createdAtAfterOrEqual,
            createdAtBefore,
            createdAtBeforeOrEqual,
        ).hashCode()

        StreamLog.d(TAG) { "[queryBannedUsers] uniqueKey: $uniqueKey" }

        return getOrCreate(uniqueKey) {
            delegate.queryBannedUsers(
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
        val uniqueKey = QueryMembersHash(channelType, channelId, offset, limit, filter, sort, members)
            .hashCode()

        StreamLog.d(TAG) { "[queryMembers] uniqueKey: $uniqueKey" }

        return getOrCreate(uniqueKey) {
            delegate.queryMembers(channelType, channelId, offset, limit, filter, sort, members)
        }
    }

    private fun <T : Any> getOrCreate(
        uniqueKey: Int,
        callBuilder: () -> Call<T>,
    ): Call<T> {
        return distinctCalls[uniqueKey] as? DistinctCall<T>
            ?: DistinctCall(scope = scope, callBuilder = callBuilder) {
                distinctCalls.remove(uniqueKey)
            }.also {
                distinctCalls[uniqueKey] = it
            }
    }

    private companion object {
        private const val TAG = "Chat:DistinctApi"
    }
}
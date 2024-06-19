package com.example.composechatsample.common

import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.cidToTypeAndId
import com.example.composechatsample.core.models.Filters
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.querysort.QuerySortByField
import com.example.composechatsample.log.taggedLogger
import com.example.composechatsample.core.Result

public class RemoteUserLookupHandler(
    private val chatClient: ChatClient,
    private val channelCid: String,
) : UserLookupHandler {

    private val logger by taggedLogger("Chat:UserLookupRemote")

    override suspend fun handleUserLookup(query: String): List<User> {
        return when {
            query.isNotEmpty() -> {
                if (DEBUG) logger.v { "[handleUserLookup] search remotely" }
                chatClient.queryMembersByUsername(channelCid = channelCid, query = query)
            }
            else -> {
                if (DEBUG) logger.v { "[handleUserLookup] #empty; query: $query" }
                emptyList()
            }
        }
    }


    private suspend fun ChatClient.queryMembersByUsername(
        channelCid: String,
        query: String,
    ): List<User> {
        if (DEBUG) logger.d { "[queryMembersByUsername] query: \"$query\"" }
        val (channelType, channelId) = channelCid.cidToTypeAndId()
        val result = queryMembers(
            channelType = channelType,
            channelId = channelId,
            offset = QUERY_MEMBERS_REQUEST_OFFSET,
            limit = QUERY_MEMBERS_REQUEST_LIMIT,
            filter = Filters.autocomplete(fieldName = "name", value = query),
            sort = QuerySortByField.ascByName(fieldName = "name"),
            members = listOf(),
        ).await()

        return when (result) {
            is Result.Success -> {
                if (DEBUG) logger.v { "[queryMembersByUsername] found ${result.value.size} users" }
                result.value.map { it.user }.filter { it.name.contains(query, true) }
            }
            is Result.Failure -> {
                logger.e { "[queryMembersByUsername] failed: ${result.value.message}" }
                emptyList()
            }
        }
    }

    private companion object {
        private const val DEBUG = false
        private const val QUERY_MEMBERS_REQUEST_OFFSET: Int = 0
        private const val QUERY_MEMBERS_REQUEST_LIMIT: Int = 30
    }
}
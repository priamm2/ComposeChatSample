package com.example.composechatsample.common

import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.cidToTypeAndId
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.state
import com.example.composechatsample.log.taggedLogger

public class LocalUserLookupHandler @JvmOverloads constructor(
    private val chatClient: ChatClient,
    private val channelCid: String,
    private val filter: QueryFilter<User> = DefaultQueryFilter { it.name.ifBlank { it.id } },
) : UserLookupHandler {

    private val logger by taggedLogger("Chat:UserLookupLocal")

    override suspend fun handleUserLookup(query: String): List<User> {
        try {
            if (DEBUG) logger.d { "[handleUserLookup] query: \"$query\"" }
            val (channelType, channelId) = channelCid.cidToTypeAndId()
            val channelState = chatClient.state.channel(channelType, channelId)
            val localUsers = channelState.members.value.map { it.user }
            val membersCount = channelState.membersCount.value
            return when (membersCount == localUsers.size) {
                true -> filter.filter(localUsers, query).also {
                    if (DEBUG) logger.v { "[handleUserLookup] found ${it.size} users" }
                }
                else -> {
                    if (DEBUG) logger.v { "[handleUserLookup] #empty; users: ${localUsers.size} out of $membersCount" }
                    emptyList()
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "[handleUserLookup] failed: $e" }
            return emptyList()
        }
    }

    private companion object {
        private const val DEBUG = false
    }
}
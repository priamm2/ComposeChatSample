package com.example.composechatsample.common

import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.models.User
import com.example.composechatsample.log.taggedLogger

public class DefaultUserLookupHandler(
    private val localHandler: UserLookupHandler,
    private val remoteHandler: UserLookupHandler,
) : UserLookupHandler {

    @JvmOverloads
    public constructor(
        chatClient: ChatClient,
        channelCid: String,
        localFilter: QueryFilter<User> = DefaultQueryFilter { it.name.ifBlank { it.id } },
    ) : this(
        localHandler = LocalUserLookupHandler(chatClient, channelCid, localFilter),
        remoteHandler = RemoteUserLookupHandler(chatClient, channelCid),
    )

    private val logger by taggedLogger("Chat:UserLookupHandler")

    override suspend fun handleUserLookup(query: String): List<User> {
        logger.d { "[handleUserLookup] query: \"$query\"" }
        return localHandler.handleUserLookup(query).ifEmpty {
            logger.v { "[handleUserLookup] no local results" }
            remoteHandler.handleUserLookup(query)
        }.also {
            logger.v { "[handleUserLookup] found ${it.size} users" }
        }
    }
}

package com.example.composechatsample.core.repository

import com.example.composechatsample.core.api.QueryChannelRequest
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.ChannelConfig
import com.example.composechatsample.core.plugin.QueryChannelListener
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.onSuccessSuspend

internal class QueryChannelListenerDatabase(private val repos: RepositoryFacade) :
    QueryChannelListener {

    override suspend fun onQueryChannelPrecondition(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onQueryChannelRequest(channelType: String, channelId: String, request: QueryChannelRequest) {

    }


    override suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        result.onSuccessSuspend { channel ->
            repos.insertChannelConfig(ChannelConfig(channel.type, channel.config))
            repos.storeStateForChannel(channel)
        }
    }
}
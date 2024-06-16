package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.api.QueryChannelRequest
import com.example.composechatsample.log.taggedLogger
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.stringify

internal class QueryChannelListenerState(private val logic: LogicRegistry) : QueryChannelListener {

    private val logger by taggedLogger("QueryChannelListenerS")

    override suspend fun onQueryChannelPrecondition(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onQueryChannelRequest(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        logger.d { "[onQueryChannelRequest] cid: $channelType:$channelId, request: $request" }
        logic.channel(channelType, channelId).updateStateFromDatabase(request)
    }

    override suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        logger.d {
            "[onQueryChannelResult] cid: $channelType:$channelId, " +
                "request: $request, result: ${result.stringify { it.cid }}"
        }
        val channelStateLogic = logic.channel(channelType, channelId).stateLogic()

        result.onSuccess { channel -> channelStateLogic.propagateChannelQuery(channel, request) }
            .onError(channelStateLogic::propagateQueryError)
    }
}
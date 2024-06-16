package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.api.QueryChannelsRequest
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.api.AnyChannelPaginationRequest
import com.example.composechatsample.core.api.QueryChannelsPaginationRequest
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.toAnyChannelPaginationRequest

internal class QueryChannelsListenerState(
    private val logicProvider: LogicRegistry,
    private val queryingChannelsFree: MutableStateFlow<Boolean>,
) : QueryChannelsListener {

    override suspend fun onQueryChannelsPrecondition(request: QueryChannelsRequest): Result<Unit> {
        return Result.Success(Unit)
    }

    override suspend fun onQueryChannelsRequest(request: QueryChannelsRequest) {
        queryingChannelsFree.value = false
        logicProvider.queryChannels(request).run {
            setCurrentRequest(request)
            queryOffline(request.toPagination())
        }
    }

    override suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        logicProvider.queryChannels(request).onQueryChannelsResult(result, request)
        queryingChannelsFree.value = true
    }

    private companion object {
        private fun QueryChannelsRequest.toPagination(): AnyChannelPaginationRequest =
            QueryChannelsPaginationRequest(
                sort = querySort,
                channelLimit = limit,
                channelOffset = offset,
                messageLimit = messageLimit,
                memberLimit = memberLimit,
            ).toAnyChannelPaginationRequest()
    }
}
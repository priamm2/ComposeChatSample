package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.api.QueryChannelsRequest
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.Result

public interface QueryChannelsListener {

    public suspend fun onQueryChannelsPrecondition(
        request: QueryChannelsRequest,
    ): Result<Unit>

    public suspend fun onQueryChannelsRequest(request: QueryChannelsRequest)

    public suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest)
}
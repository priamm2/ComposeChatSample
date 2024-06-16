package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.api.QueryChannelRequest
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.Result

public interface QueryChannelListener {

    public suspend fun onQueryChannelPrecondition(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Result<Unit>


    public suspend fun onQueryChannelRequest(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    )

    public suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    )
}
package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.api.QueryChannelsRequest
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.Result

public object ChannelFilterRequest {
    public suspend fun ChatClient.filterWithOffset(
        filter: FilterObject,
        offset: Int,
        limit: Int,
    ): List<Channel> {
        val request = QueryChannelsRequest(
            filter = filter,
            offset = offset,
            limit = limit,
            messageLimit = 0,
            memberLimit = 0,
        )
        return queryChannelsInternal(request).await().let { result ->
            when (result) {
                is Result.Success -> result.value
                is Result.Failure -> emptyList()
            }
        }
    }
}
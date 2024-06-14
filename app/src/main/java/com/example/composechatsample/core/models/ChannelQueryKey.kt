package com.example.composechatsample.core.models

import com.example.composechatsample.core.models.requests.QueryChannelRequest


internal data class ChannelQueryKey(
    val channelType: String,
    val channelId: String,
    val queryKey: QueryChannelRequest,
) {

    companion object {
        fun from(
            channelType: String,
            channelId: String,
            query: com.example.composechatsample.core.api.QueryChannelRequest,
        ): ChannelQueryKey {
            return ChannelQueryKey(
                channelType = channelType,
                channelId = channelId,
                queryKey = QueryChannelRequest(
                    state = query.state,
                    watch = query.watch,
                    presence = query.presence,
                    messages = query.messages,
                    watchers = query.watchers,
                    members = query.members,
                    data = query.data,
                ),
            )
        }
    }
}
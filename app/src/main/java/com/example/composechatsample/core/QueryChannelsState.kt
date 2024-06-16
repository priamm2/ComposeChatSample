package com.example.composechatsample.core;

import com.example.composechatsample.core.api.QueryChannelsRequest
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.querysort.QuerySorter
import com.example.composechatsample.core.plugin.ChannelsStateData
import com.example.composechatsample.core.plugin.ChatEventHandlerFactory
import kotlinx.coroutines.flow.StateFlow

public interface QueryChannelsState {
    public val recoveryNeeded: StateFlow<Boolean>
    public val filter: FilterObject
    public val sort: QuerySorter<Channel>
    public val currentRequest: StateFlow<QueryChannelsRequest?>
    public val nextPageRequest: StateFlow<QueryChannelsRequest?>
    public val loading: StateFlow<Boolean>
    public val loadingMore: StateFlow<Boolean>
    public val endOfChannels: StateFlow<Boolean>
    public val channels: StateFlow<List<Channel>?>
    public val channelsStateData: StateFlow<ChannelsStateData>
    public var chatEventHandlerFactory: ChatEventHandlerFactory?
}
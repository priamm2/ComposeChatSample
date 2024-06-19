package com.example.composechatsample.screen.channels

public data class ChannelsState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val endOfChannels: Boolean = false,
    val channelItems: List<ItemState> = emptyList(),
    val searchQuery: SearchQuery = SearchQuery.Empty,
)
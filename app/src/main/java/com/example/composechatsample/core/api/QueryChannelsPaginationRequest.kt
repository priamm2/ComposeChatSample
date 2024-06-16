package com.example.composechatsample.core.api

import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.querysort.QuerySorter

data class QueryChannelsPaginationRequest(
    val sort: QuerySorter<Channel>,
    val channelOffset: Int = 0,
    val channelLimit: Int = 30,
    val messageLimit: Int = 10,
    val memberLimit: Int,
) {

    val isFirstPage: Boolean
        get() = channelOffset == 0
}
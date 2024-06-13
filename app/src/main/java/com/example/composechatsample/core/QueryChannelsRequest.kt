package com.example.composechatsample.core

import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.querysort.QuerySortByField
import com.example.composechatsample.core.models.querysort.QuerySorter

public data class QueryChannelsRequest(
    public val filter: FilterObject,
    public var offset: Int = 0,
    public var limit: Int,
    public val querySort: QuerySorter<Channel> = QuerySortByField(),
    public var messageLimit: Int = 0,
    public var memberLimit: Int = 1,
) : ChannelRequest<QueryChannelsRequest> {

    override var state: Boolean = true
    override var watch: Boolean = true
    override var presence: Boolean = false

    public val sort: List<Map<String, Any>> = querySort.toDto()

    public fun withMessages(limit: Int): QueryChannelsRequest {
        messageLimit = limit
        return this
    }

    public fun withLimit(limit: Int): QueryChannelsRequest {
        this.limit = limit
        return this
    }

    public fun withOffset(offset: Int): QueryChannelsRequest {
        this.offset = offset
        return this
    }

    public val isFirstPage: Boolean
        get() = offset == 0
}
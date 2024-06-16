package com.example.composechatsample.core.repository

import com.example.composechatsample.core.QueryChannelsSpec
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.querysort.QuerySorter

internal object NoOpQueryChannelsRepository : QueryChannelsRepository {
    override suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec) { /* No-Op */ }
    override suspend fun selectBy(filter: FilterObject, querySort: QuerySorter<Channel>): QueryChannelsSpec? = null
    override suspend fun clear() { /* No-Op */ }
}
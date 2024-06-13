package com.example.composechatsample.core.repository

import com.example.composechatsample.core.QueryChannelsSpec
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.querysort.QuerySorter

public interface QueryChannelsRepository {

    public suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec)

    public suspend fun selectBy(filter: FilterObject, querySort: QuerySorter<Channel>): QueryChannelsSpec?


    public suspend fun clear()
}
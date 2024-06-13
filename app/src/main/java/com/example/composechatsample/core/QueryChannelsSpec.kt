package com.example.composechatsample.core

import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.querysort.QuerySorter

public data class QueryChannelsSpec(
    val filter: FilterObject,
    val querySort: QuerySorter<Channel>,
) {
    var cids: Set<String> = emptySet()
}
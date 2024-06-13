package com.example.composechatsample.core

import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.querysort.QuerySorter

public data class SearchMessagesRequest @JvmOverloads constructor(
    val offset: Int?,
    val limit: Int?,
    val channelFilter: FilterObject,
    val messageFilter: FilterObject,
    val next: String? = null,
    val querySort: QuerySorter<Message>? = null,
) {
    val sort: List<Map<String, Any>>? = querySort?.toDto()
}
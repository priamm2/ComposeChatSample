package com.example.composechatsample.core.models

import com.example.composechatsample.core.models.querysort.QuerySorter

internal data class QueryMembersHash(
    val channelType: String,
    val channelId: String,
    val offset: Int,
    val limit: Int,
    val filter: FilterObject,
    val sort: QuerySorter<Member>,
    val members: List<Member>,
)
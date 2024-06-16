package com.example.composechatsample.core.api

import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.querysort.QuerySortByField
import com.example.composechatsample.core.models.querysort.QuerySorter

public data class QueryUsersRequest @JvmOverloads constructor(
    var filter: FilterObject,
    val offset: Int,
    val limit: Int,
    var querySort: QuerySorter<User> = QuerySortByField(),
    var presence: Boolean = false,
) {
    val sort: List<Map<String, Any>> = querySort.toDto()
}
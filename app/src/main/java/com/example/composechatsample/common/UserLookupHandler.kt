package com.example.composechatsample.common

import com.example.composechatsample.core.models.User

public fun interface UserLookupHandler {

    public suspend fun handleUserLookup(query: String): List<User>
}

public fun UserLookupHandler.withQueryFormatter(
    queryFormatter: QueryFormatter,
): UserLookupHandler {
    val delegate = this
    return UserLookupHandler { query ->
        val updatedQuery = queryFormatter.format(query)
        delegate.handleUserLookup(updatedQuery)
    }
}
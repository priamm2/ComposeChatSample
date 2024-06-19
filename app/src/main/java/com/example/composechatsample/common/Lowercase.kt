package com.example.composechatsample.common

internal class Lowercase : QueryFormatter {
    override fun format(query: String): String {
        if (query.isEmpty()) return query
        return query.lowercase()
    }
}
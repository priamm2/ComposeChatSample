package com.example.composechatsample.screen

sealed class SearchQuery {
    abstract val query: String

    data class Channels(override val query: String) : SearchQuery()

    data class Messages(override val query: String) : SearchQuery()

    data object Empty : SearchQuery() {
        override val query: String = ""
    }
}
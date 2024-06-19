package com.example.composechatsample.screen.channels

public sealed class SearchQuery {
    public abstract val query: String
    public data class Channels(override val query: String) : SearchQuery()

    public data class Messages(override val query: String) : SearchQuery()
    public data object Empty : SearchQuery() {
        override val query: String = ""
    }
}
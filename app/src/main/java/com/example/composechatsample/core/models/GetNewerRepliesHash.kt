package com.example.composechatsample.core.models

internal data class GetNewerRepliesHash(
    val parentId: String,
    val limit: Int,
    val lastId: String?,
)
package com.example.composechatsample.core.models

internal data class GetReactionsHash(
    val messageId: String,
    val offset: Int,
    val limit: Int,
)
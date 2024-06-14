package com.example.composechatsample.core.models

internal data class GetRepliesHash(
    val messageId: String,
    val firstId: String?,
    val limit: Int,
)
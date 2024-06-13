package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable

@Immutable
public data class SearchMessagesResult(
    val messages: List<Message> = emptyList(),
    val next: String? = null,
    val previous: String? = null,
    val resultsWarning: SearchWarning? = null,
)

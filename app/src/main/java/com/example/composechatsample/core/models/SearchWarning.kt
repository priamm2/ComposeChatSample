package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable

@Immutable
public data class SearchWarning(
    val channelSearchCids: List<String>,
    val channelSearchCount: Int,
    val warningCode: Int,
    val warningDescription: String,
)

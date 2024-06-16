package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchWarningDto(
    val channel_search_cids: List<String>,
    val channel_search_count: Int,
    val warning_code: Int,
    val warning_description: String,
)
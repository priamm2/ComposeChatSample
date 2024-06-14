package com.example.composechatsample.core.models.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SearchMessagesResponse(
    val results: List<MessageResponse>,
    val next: String?,
    val previous: String?,
    val resultsWarning: SearchWarningDto?,
)
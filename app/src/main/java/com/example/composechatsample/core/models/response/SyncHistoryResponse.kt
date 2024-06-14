package com.example.composechatsample.core.models.response

import com.example.composechatsample.core.models.dto.ChatEventDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SyncHistoryResponse(
    val events: List<ChatEventDto>,
)
package com.example.composechatsample.core.models.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SyncHistoryRequest(
    val channel_cids: List<String>,
    val last_sync_at: String,
)
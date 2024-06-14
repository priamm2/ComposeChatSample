package com.example.composechatsample.core.models.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class PartialUpdateMessageRequest(
    val set: Map<String, Any>,
    val unset: List<String>,
    val skip_enrich_url: Boolean = false,
)
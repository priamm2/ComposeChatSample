package com.example.composechatsample.core.models.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class QueryUsersRequest(
    val filter_conditions: Map<*, *>,
    val offset: Int,
    val limit: Int,
    val sort: List<Map<String, Any>>,
    val presence: Boolean,
)
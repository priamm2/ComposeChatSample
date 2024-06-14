package com.example.composechatsample.core.models.requests

import com.example.composechatsample.core.models.dto.UpstreamMemberDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class QueryMembersRequest(
    val type: String,
    val id: String,
    val filter_conditions: Map<*, *>,
    val offset: Int,
    val limit: Int,
    val sort: List<Map<String, Any>>,
    val members: List<UpstreamMemberDto>,
)
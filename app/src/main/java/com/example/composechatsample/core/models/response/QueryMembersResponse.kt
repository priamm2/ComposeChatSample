package com.example.composechatsample.core.models.response

import com.example.composechatsample.core.models.dto.DownstreamMemberDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class QueryMembersResponse(
    val members: List<DownstreamMemberDto>,
)
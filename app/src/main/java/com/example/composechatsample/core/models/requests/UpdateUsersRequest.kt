package com.example.composechatsample.core.models.requests

import com.example.composechatsample.core.models.dto.UpstreamUserDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UpdateUsersRequest(
    val users: Map<String, UpstreamUserDto>,
)
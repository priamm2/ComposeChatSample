package com.example.composechatsample.core.models.response

import com.example.composechatsample.core.models.dto.DownstreamUserDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UsersResponse(
    val users: List<DownstreamUserDto>,
)
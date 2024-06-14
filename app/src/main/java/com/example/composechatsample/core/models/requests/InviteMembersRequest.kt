package com.example.composechatsample.core.models.requests

import com.example.composechatsample.core.models.dto.UpstreamMessageDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class InviteMembersRequest(
    val invites: List<String>,
    val message: UpstreamMessageDto?,
    val skip_push: Boolean?,
)
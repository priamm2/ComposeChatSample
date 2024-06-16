package com.example.composechatsample.core.models.response

import com.example.composechatsample.core.models.dto.DownstreamMuteDto
import com.example.composechatsample.core.models.dto.DownstreamUserDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class MuteUserResponse(
    val mute: DownstreamMuteDto,
    val own_user: DownstreamUserDto,
)
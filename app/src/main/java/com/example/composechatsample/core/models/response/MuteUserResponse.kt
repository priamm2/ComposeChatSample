package com.example.composechatsample.core.models.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class MuteUserResponse(
    val mute: DownstreamMuteDto,
    val own_user: DownstreamUserDto,
)
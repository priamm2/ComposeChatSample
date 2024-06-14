package com.example.composechatsample.core.models.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class MuteChannelRequest(
    val channel_cid: String,
    val expiration: Int?,
)

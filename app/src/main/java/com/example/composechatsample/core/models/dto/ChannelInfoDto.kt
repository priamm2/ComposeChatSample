package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChannelInfoDto(
    val cid: String?,
    val id: String?,
    val member_count: Int = 0,
    val name: String?,
    val type: String?,
    val image: String?,
)
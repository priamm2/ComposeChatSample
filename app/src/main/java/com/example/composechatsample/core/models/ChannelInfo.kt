package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable

@Immutable
public data class ChannelInfo(
    val cid: String? = null,
    val id: String? = null,
    val type: String? = null,
    val memberCount: Int = 0,
    val name: String? = null,
    val image: String? = null,
)

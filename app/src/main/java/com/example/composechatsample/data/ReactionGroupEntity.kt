package com.example.composechatsample.data

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class ReactionGroupEntity(
    val type: String,
    val count: Int,
    val sumScore: Int,
    val firstReactionAt: Date,
    val lastReactionAt: Date,
)
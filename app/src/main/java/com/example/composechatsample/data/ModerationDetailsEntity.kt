package com.example.composechatsample.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ModerationDetailsEntity(
    val originalText: String,
    val action: String,
    val errorMsg: String,
)
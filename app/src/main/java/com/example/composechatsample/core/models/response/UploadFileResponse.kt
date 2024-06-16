package com.example.composechatsample.core.models.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadFileResponse(
    val file: String,
    val thumb_url: String?,
)
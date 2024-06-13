package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable

@Immutable
public data class UploadedFile @JvmOverloads constructor(
    val file: String,
    val thumbUrl: String? = null,
    val extraData: Map<String, Any> = emptyMap(),
)

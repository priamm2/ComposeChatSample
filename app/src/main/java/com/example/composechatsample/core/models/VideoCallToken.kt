package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable

@Immutable
data class VideoCallToken(
    val token: String,
    val agoraUid: Int?,
    val agoraAppId: String?,
)

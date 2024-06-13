package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable
import com.example.composechatsample.core.models.AgoraChannel
import com.example.composechatsample.core.models.HMSRoom

@Immutable
data class VideoCallInfo(
    val callId: String,
    val provider: String,
    val type: String,
    val agoraChannel: AgoraChannel,
    val hmsRoom: HMSRoom,
    val videoCallToken: VideoCallToken,
)

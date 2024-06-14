package com.example.composechatsample.core.models.requests

public data class SendActionRequest(
    val channelId: String,
    val messageId: String,
    val type: String,
    val formData: Map<Any, Any>,
)
package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Message

public interface SendAttachmentListener {

    public suspend fun onAttachmentSendRequest(
        channelType: String,
        channelId: String,
        message: Message,
    )
}
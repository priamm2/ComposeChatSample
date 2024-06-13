package com.example.composechatsample.core

import com.example.composechatsample.core.models.Message

interface ChannelMessagesUpdateLogic {

    fun upsertMessage(message: Message, updateCount: Boolean = true)

    fun upsertMessages(
        messages: List<Message>,
        shouldRefreshMessages: Boolean = false,
        updateCount: Boolean = true,
    )

    fun listenForChannelState(): ChannelState

    fun replyMessage(repliedMessage: Message?)
}
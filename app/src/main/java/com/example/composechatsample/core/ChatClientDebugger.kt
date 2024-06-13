package com.example.composechatsample.core

import com.example.composechatsample.core.models.Message

public interface ChatClientDebugger {

    public fun onNonFatalErrorOccurred(tag: String, src: String, desc: String, error: Error) {}

    public fun debugSendMessage(
        channelType: String,
        channelId: String,
        message: Message,
        isRetrying: Boolean = false,
    ): SendMessageDebugger = StubSendMessageDebugger
}

internal object StubChatClientDebugger : ChatClientDebugger
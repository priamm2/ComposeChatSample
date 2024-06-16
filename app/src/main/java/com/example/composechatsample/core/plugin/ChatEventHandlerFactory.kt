package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.state.ClientState
import kotlinx.coroutines.flow.StateFlow

public open class ChatEventHandlerFactory(
    private val clientState: ClientState = ChatClient.instance().clientState,
) {


    public open fun chatEventHandler(channels: StateFlow<Map<String, Channel>?>): ChatEventHandler {
        return DefaultChatEventHandler(channels = channels, clientState = clientState)
    }
}
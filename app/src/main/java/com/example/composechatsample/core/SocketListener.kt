package com.example.composechatsample.core

import com.example.composechatsample.core.events.ChatEvent
import com.example.composechatsample.core.events.ConnectedEvent

public open class SocketListener {
    public open val deliverOnMainThread: Boolean = true
    public open fun onConnecting() {
    }
    public open fun onConnected(event: ConnectedEvent) {
    }

    public open fun onDisconnected(cause: DisconnectCause) {
    }

    public open fun onError(error: Error) {
    }

    public open fun onEvent(event: ChatEvent) {
    }
}
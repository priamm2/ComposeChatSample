package com.example.composechatsample.core

import com.example.composechatsample.core.models.Message

public interface SendMessageDebugger {
    public fun onStart(message: Message) {}

    public fun onInterceptionStart(message: Message) {}

    public fun onInterceptionUpdate(message: Message) {}

    public fun onInterceptionStop(result: Result<Message>, message: Message) {}

    public fun onSendStart(message: Message) {}

    public fun onSendStop(result: Result<Message>, message: Message) {}

    public fun onStop(result: Result<Message>, message: Message) {}
}

internal object StubSendMessageDebugger : SendMessageDebugger
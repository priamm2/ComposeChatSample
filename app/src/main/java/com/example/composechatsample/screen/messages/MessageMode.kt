package com.example.composechatsample.screen.messages

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.state.ThreadState

public sealed class MessageMode {

    public object Normal : MessageMode() { override fun toString(): String = "Normal" }


    public data class MessageThread @JvmOverloads constructor(
        public val parentMessage: Message,
        public val threadState: ThreadState? = null,
    ) : MessageMode()
}

internal fun MessageMode.stringify(): String = when (this) {
    MessageMode.Normal -> "Normal"
    is MessageMode.MessageThread -> "MessageThread(parentMessage.id=${parentMessage.id})"
}
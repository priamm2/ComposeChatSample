package com.example.composechatsample.screen

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Reaction

public sealed class MessageAction {
    public abstract val message: Message
}

public data class React(
    public val reaction: Reaction,
    override val message: Message,
) : MessageAction()

public data class Resend(
    override val message: Message,
) : MessageAction()

public data class Reply(
    override val message: Message,
) : MessageAction()

public data class ThreadReply(
    override val message: Message,
) : MessageAction()

public data class Copy(
    override val message: Message,
) : MessageAction()

public data class MarkAsUnread(
    override val message: Message,
) : MessageAction()

public data class Edit(
    override val message: Message,
) : MessageAction()

public data class Pin(
    override val message: Message,
) : MessageAction()

public data class Delete(
    override val message: Message,
) : MessageAction()

public data class Flag(
    override val message: Message,
) : MessageAction()

public data class CustomAction(
    override val message: Message,
    public val extraProperties: Map<String, Any> = emptyMap(),
) : MessageAction()

public fun MessageAction.updateMessage(message: Message): MessageAction {
    return when (this) {
        is React -> copy(message = message)
        is Resend -> copy(message = message)
        is Reply -> copy(message = message)
        is ThreadReply -> copy(message = message)
        is Copy -> copy(message = message)
        is MarkAsUnread -> copy(message = message)
        is Edit -> copy(message = message)
        is Pin -> copy(message = message)
        is Delete -> copy(message = message)
        is Flag -> copy(message = message)
        is CustomAction -> copy(message = message)
    }
}
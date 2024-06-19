package com.example.composechatsample.screen

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.MessageType
import com.example.composechatsample.core.models.Reaction
import java.util.Date

internal object PreviewMessageData {

    val message1: Message = Message(
        id = "message-id-1",
        text = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.",
        createdAt = Date(),
        type = MessageType.REGULAR,
    )

    val message2: Message = Message(
        id = "message-id-2",
        text = "Aenean commodo ligula eget dolor.",
        createdAt = Date(),
        type = MessageType.REGULAR,
    )

    val messageWithOwnReaction: Message = Message(
        id = "message-id-3",
        text = "Pellentesque leo dui, finibus et nibh et, congue aliquam lectus",
        createdAt = Date(),
        type = MessageType.REGULAR,
        ownReactions = mutableListOf(Reaction(messageId = "message-id-3", type = "haha")),
    )
}
package com.example.composechatsample.screen.messages

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.composechatsample.core.isMine
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun QuotedMessageContent(
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
    replyMessage: Message? = null,
    attachmentContent: @Composable (Message) -> Unit = { DefaultQuotedMessageAttachmentContent(it) },
    textContent: @Composable (Message) -> Unit = {
        DefaultQuotedMessageTextContent(
            message = it,
            replyMessage = replyMessage,
            currentUser = currentUser,
        )
    },
) {
    val messageBubbleShape = if (message.isMine(currentUser)) {
        ChatTheme.shapes.myMessageBubble
    } else {
        ChatTheme.shapes.otherMessageBubble
    }

    val messageBubbleColor = if (replyMessage?.isMine(currentUser) != false) {
        ChatTheme.ownMessageTheme.quotedBackgroundColor
    } else {
        ChatTheme.otherMessageTheme.quotedBackgroundColor
    }

    MessageBubble(
        modifier = modifier,
        shape = messageBubbleShape,
        color = messageBubbleColor,
        content = {
            Row {
                attachmentContent(message)

                textContent(message)
            }
        },
    )
}

@Composable
internal fun DefaultQuotedMessageAttachmentContent(message: Message) {
    if (message.attachments.isNotEmpty()) {
        QuotedMessageAttachmentContent(
            message = message,
            onLongItemClick = {},
        )
    }
}

@Composable
internal fun DefaultQuotedMessageTextContent(
    message: Message,
    currentUser: User?,
    replyMessage: Message? = null,
) {
    QuotedMessageText(
        message = message,
        replyMessage = replyMessage,
        currentUser = currentUser,
    )
}
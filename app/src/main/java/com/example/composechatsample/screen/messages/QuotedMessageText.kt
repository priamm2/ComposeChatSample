package com.example.composechatsample.screen.messages

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextOverflow
import com.example.composechatsample.core.isFewEmoji
import com.example.composechatsample.core.isMine
import com.example.composechatsample.core.isSingleEmoji
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun QuotedMessageText(
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
    replyMessage: Message? = null,
    quoteMaxLines: Int = DefaultQuoteMaxLines,
) {
    val style = when {
        message.isSingleEmoji() -> ChatTheme.typography.singleEmoji
        message.isFewEmoji() -> ChatTheme.typography.emojiOnly
        else -> when (replyMessage?.isMine(currentUser) != false) {
            true -> ChatTheme.ownMessageTheme.textStyle
            else -> ChatTheme.otherMessageTheme.textStyle
        }
    }


    val styledText = ChatTheme.quotedMessageTextFormatter.format(message, replyMessage, currentUser)

    val horizontalPadding = ChatTheme.dimens.quotedMessageTextHorizontalPadding
    val verticalPadding = ChatTheme.dimens.quotedMessageTextVerticalPadding

    Text(
        modifier = modifier
            .padding(
                horizontal = horizontalPadding,
                vertical = verticalPadding,
            )
            .clipToBounds(),
        text = styledText,
        style = style,
        maxLines = quoteMaxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

private const val DefaultQuoteMaxLines: Int = 3
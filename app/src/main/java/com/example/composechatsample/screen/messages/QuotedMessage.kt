package com.example.composechatsample.screen.messages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.composechatsample.core.initials
import com.example.composechatsample.core.isMine
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.screen.components.Avatar
import com.example.composechatsample.ui.theme.ChatTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun QuotedMessage(
    message: Message,
    currentUser: User?,
    onLongItemClick: (Message) -> Unit,
    onQuotedMessageClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    replyMessage: Message? = null,
    leadingContent: @Composable (Message) -> Unit = {
        DefaultQuotedMessageLeadingContent(
            message = it,
            currentUser = currentUser,
        )
    },
    centerContent: @Composable RowScope.(Message) -> Unit = {
        DefaultQuotedMessageCenterContent(
            message = it,
            replyMessage = replyMessage,
            currentUser = currentUser,
        )
    },
    trailingContent: @Composable (Message) -> Unit = {
        DefaultQuotedMessageTrailingContent(
            message = it,
            currentUser = currentUser,
        )
    },
) {
    Row(
        modifier = modifier.combinedClickable(
            interactionSource = MutableInteractionSource(),
            indication = null,
            onLongClick = { onLongItemClick(message) },
            onClick = { onQuotedMessageClick(message) },
        ),
        verticalAlignment = Alignment.Bottom,
    ) {
        leadingContent(message)

        centerContent(message)

        trailingContent(message)
    }
}

@Composable
internal fun DefaultQuotedMessageLeadingContent(
    message: Message,
    currentUser: User?,
) {
    if (!message.isMine(currentUser)) {
        Avatar(
            modifier = Modifier
                .padding(start = 2.dp)
                .size(24.dp),
            imageUrl = message.user.image,
            initials = message.user.initials,
            textStyle = ChatTheme.typography.captionBold,
        )

        Spacer(modifier = Modifier.size(8.dp))
    }
}

@Composable
internal fun DefaultQuotedMessageTrailingContent(
    message: Message,
    currentUser: User?,
) {
    if (message.isMine(currentUser)) {
        Spacer(modifier = Modifier.size(8.dp))

        Avatar(
            modifier = Modifier
                .padding(start = 2.dp)
                .size(24.dp),
            imageUrl = message.user.image,
            initials = message.user.initials,
            textStyle = ChatTheme.typography.captionBold,
        )
    }
}

@Composable
public fun RowScope.DefaultQuotedMessageCenterContent(
    message: Message,
    currentUser: User?,
    replyMessage: Message? = null,
) {
    QuotedMessageContent(
        message = message,
        replyMessage = replyMessage,
        modifier = Modifier.weight(1f, fill = false),
        currentUser = currentUser,
    )
}
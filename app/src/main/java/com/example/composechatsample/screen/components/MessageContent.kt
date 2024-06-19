package com.example.composechatsample.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.core.isDeleted
import com.example.composechatsample.core.isGiphyEphemeral
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.screen.DefaultMessageTextContent
import com.example.composechatsample.screen.GiphyAction
import com.example.composechatsample.screen.MediaGalleryPreviewResult
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun MessageContent(
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    giphyEphemeralContent: @Composable () -> Unit = {
        DefaultMessageGiphyContent(
            message = message,
            onGiphyActionClick = onGiphyActionClick,
        )
    },
    deletedMessageContent: @Composable () -> Unit = {
        DefaultMessageDeletedContent(modifier = modifier)
    },
    regularMessageContent: @Composable () -> Unit = {
        DefaultMessageContent(
            message = message,
            currentUser = currentUser,
            onLongItemClick = onLongItemClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    },
) {
    when {
        message.isGiphyEphemeral() -> giphyEphemeralContent()
        message.isDeleted() -> deletedMessageContent()
        else -> regularMessageContent()
    }
}

@Composable
internal fun DefaultMessageGiphyContent(
    message: Message,
    onGiphyActionClick: (GiphyAction) -> Unit,
) {
    GiphyMessageContent(
        message = message,
        onGiphyActionClick = onGiphyActionClick,
    )
}

@Composable
internal fun DefaultMessageDeletedContent(
    modifier: Modifier,
) {
    Text(
        modifier = modifier
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = 8.dp,
            ),
        text = stringResource(id = R.string.stream_compose_message_deleted),
        color = ChatTheme.colors.textLowEmphasis,
        style = ChatTheme.typography.footnoteItalic,
    )
}

@Composable
internal fun DefaultMessageContent(
    message: Message,
    currentUser: User?,
    onLongItemClick: (Message) -> Unit,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit,
) {
    Column {
        MessageAttachmentsContent(
            message = message,
            onLongItemClick = onLongItemClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
        )

        if (message.text.isNotEmpty()) {
            DefaultMessageTextContent(
                message = message,
                currentUser = currentUser,
                onLongItemClick = onLongItemClick,
                onQuotedMessageClick = onQuotedMessageClick,
            )
        }
    }
}
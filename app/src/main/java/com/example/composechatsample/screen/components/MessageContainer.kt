package com.example.composechatsample.screen.components

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.ReactionSorting
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun MessageContainer(
    messageListItemState: MessageListItemState,
    reactionSorting: ReactionSorting,
    onLongItemClick: (Message) -> Unit = {},
    onReactionsClick: (Message) -> Unit = {},
    onThreadClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    dateSeparatorContent: @Composable (DateSeparatorItemState) -> Unit = {
        DefaultMessageDateSeparatorContent(dateSeparator = it)
    },
    unreadSeparatorContent: @Composable (UnreadSeparatorItemState) -> Unit = {
        DefaultMessageUnreadSeparatorContent(unreadSeparatorItemState = it)
    },
    threadSeparatorContent: @Composable (ThreadDateSeparatorItemState) -> Unit = {
        DefaultMessageThreadSeparatorContent(threadSeparator = it)
    },
    systemMessageContent: @Composable (SystemMessageItemState) -> Unit = {
        DefaultSystemMessageContent(systemMessageState = it)
    },
    messageItemContent: @Composable (MessageItemState) -> Unit = {
        DefaultMessageItem(
            messageItem = it,
            reactionSorting = reactionSorting,
            onLongItemClick = onLongItemClick,
            onReactionsClick = onReactionsClick,
            onThreadClick = onThreadClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    },
    typingIndicatorContent: @Composable (TypingItemState) -> Unit = { },
    emptyThreadPlaceholderItemContent: @Composable (EmptyThreadPlaceholderItemState) -> Unit = { },
    startOfTheChannelItemState: @Composable (StartOfTheChannelItemState) -> Unit = { },
) {
    when (messageListItemState) {
        is DateSeparatorItemState -> dateSeparatorContent(messageListItemState)
        is ThreadDateSeparatorItemState -> threadSeparatorContent(messageListItemState)
        is SystemMessageItemState -> systemMessageContent(messageListItemState)
        is MessageItemState -> messageItemContent(messageListItemState)
        is TypingItemState -> typingIndicatorContent(messageListItemState)
        is EmptyThreadPlaceholderItemState -> emptyThreadPlaceholderItemContent(messageListItemState)
        is UnreadSeparatorItemState -> unreadSeparatorContent(messageListItemState)
        is StartOfTheChannelItemState -> startOfTheChannelItemState(messageListItemState)
    }
}

@Composable
internal fun DefaultMessageDateSeparatorContent(dateSeparator: DateSeparatorItemState) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .padding(vertical = 8.dp),
            color = ChatTheme.messageDateSeparatorTheme.backgroundColor,
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 16.dp),
                text = DateUtils.getRelativeTimeSpanString(
                    dateSeparator.date.time,
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE,
                ).toString(),
                style = ChatTheme.messageDateSeparatorTheme.textStyle,
            )
        }
    }
}

@Composable
internal fun DefaultMessageUnreadSeparatorContent(unreadSeparatorItemState: UnreadSeparatorItemState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.messageUnreadSeparatorTheme.backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 16.dp),
            text = LocalContext.current.resources.getString(
                R.string.stream_compose_message_list_unread_separator,
            ),
            style = ChatTheme.messageUnreadSeparatorTheme.textStyle,
        )
    }
}

@Composable
internal fun DefaultMessageThreadSeparatorContent(threadSeparator: ThreadDateSeparatorItemState) {
    val backgroundGradient = Brush.verticalGradient(
        listOf(
            ChatTheme.colors.threadSeparatorGradientStart,
            ChatTheme.colors.threadSeparatorGradientEnd,
        ),
    )
    val replyCount = threadSeparator.replyCount

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ChatTheme.dimens.threadSeparatorVerticalPadding)
            .background(brush = backgroundGradient),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.padding(vertical = ChatTheme.dimens.threadSeparatorTextVerticalPadding),
            text = LocalContext.current.resources.getQuantityString(
                R.plurals.stream_compose_message_list_thread_separator,
                replyCount,
                replyCount,
            ),
            color = ChatTheme.colors.textLowEmphasis,
            style = ChatTheme.typography.body,
        )
    }
}

@Composable
internal fun DefaultSystemMessageContent(systemMessageState: SystemMessageItemState) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        text = systemMessageState.message.text,
        color = ChatTheme.colors.textLowEmphasis,
        style = ChatTheme.typography.footnoteBold,
        textAlign = TextAlign.Center,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun DefaultMessageItem(
    messageItem: MessageItemState,
    reactionSorting: ReactionSorting,
    onLongItemClick: (Message) -> Unit,
    onReactionsClick: (Message) -> Unit = {},
    onThreadClick: (Message) -> Unit,
    onGiphyActionClick: (GiphyAction) -> Unit,
    onQuotedMessageClick: (Message) -> Unit,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
) {
    MessageItem(
        messageItem = messageItem,
        reactionSorting = reactionSorting,
        onLongItemClick = onLongItemClick,
        onReactionsClick = onReactionsClick,
        onThreadClick = onThreadClick,
        onGiphyActionClick = onGiphyActionClick,
        onQuotedMessageClick = onQuotedMessageClick,
        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
    )
}
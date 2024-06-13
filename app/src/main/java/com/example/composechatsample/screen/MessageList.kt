package com.example.composechatsample.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.ReactionSorting
import com.example.composechatsample.core.models.ReactionSortingByFirstReactionAt
import com.example.composechatsample.screen.components.MessageContainer
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.ui.theme.ThreadMessagesStart
import com.example.composechatsample.viewModel.MessageListViewModel

@Composable
public fun MessageList(
    viewModel: MessageListViewModel,
    reactionSorting: ReactionSorting = ReactionSortingByFirstReactionAt,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    messagesLazyListState: MessagesLazyListState =
        rememberMessageListState(parentMessageId = viewModel.currentMessagesState.parentMessageId),
    threadMessagesStart: ThreadMessagesStart = ThreadMessagesStart.BOTTOM,
    onThreadClick: (Message) -> Unit = { viewModel.openMessageThread(it) },
    onLongItemClick: (Message) -> Unit = { viewModel.selectMessage(it) },
    onReactionsClick: (Message) -> Unit = { viewModel.selectReactions(it) },
    onMessagesPageStartReached: () -> Unit = { viewModel.loadOlderMessages() },
    onLastVisibleMessageChanged: (Message) -> Unit = { viewModel.updateLastSeenMessage(it) },
    onScrollToBottom: () -> Unit = { viewModel.clearNewMessageState() },
    onGiphyActionClick: (GiphyAction) -> Unit = { viewModel.performGiphyAction(it) },
    onQuotedMessageClick: (Message) -> Unit = { message ->
        viewModel.scrollToMessage(
            messageId = message.id,
            parentMessageId = message.parentId,
        )
    },
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {
        if (it?.resultType == MediaGalleryPreviewResultType.SHOW_IN_CHAT) {
            viewModel.scrollToMessage(
                messageId = it.messageId,
                parentMessageId = it.parentMessageId,
            )
        }
    },
    onMessagesPageEndReached: (String) -> Unit = { viewModel.onBottomEndRegionReached(it) },
    onScrollToBottomClicked: (() -> Unit) -> Unit = { viewModel.scrollToBottom(scrollToBottom = it) },
    loadingContent: @Composable () -> Unit = { DefaultMessageListLoadingIndicator(modifier) },
    emptyContent: @Composable () -> Unit = { DefaultMessageListEmptyContent(modifier) },
    helperContent: @Composable BoxScope.() -> Unit = {
        DefaultMessagesHelperContent(
            messagesState = viewModel.currentMessagesState,
            messagesLazyListState = messagesLazyListState,
            scrollToBottom = onScrollToBottomClicked,
        )
    },
    loadingMoreContent: @Composable () -> Unit = { DefaultMessagesLoadingMoreIndicator() },
    itemContent: @Composable (MessageListItemState) -> Unit = { messageListItem ->
        DefaultMessageContainer(
            messageListItemState = messageListItem,
            reactionSorting = reactionSorting,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onThreadClick = onThreadClick,
            onLongItemClick = onLongItemClick,
            onReactionsClick = onReactionsClick,
            onGiphyActionClick = onGiphyActionClick,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    },
) {
    MessageList(
        reactionSorting = reactionSorting,
        modifier = modifier,
        contentPadding = contentPadding,
        currentState = viewModel.currentMessagesState,
        messagesLazyListState = messagesLazyListState,
        onMessagesPageStartReached = onMessagesPageStartReached,
        threadMessagesStart = threadMessagesStart,
        onLastVisibleMessageChanged = onLastVisibleMessageChanged,
        onLongItemClick = onLongItemClick,
        onReactionsClick = onReactionsClick,
        onScrolledToBottom = onScrollToBottom,
        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
        itemContent = itemContent,
        helperContent = helperContent,
        loadingMoreContent = loadingMoreContent,
        loadingContent = loadingContent,
        emptyContent = emptyContent,
        onQuotedMessageClick = onQuotedMessageClick,
        onMessagesPageEndReached = onMessagesPageEndReached,
        onScrollToBottom = onScrollToBottomClicked,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun DefaultMessageContainer(
    messageListItemState: MessageListItemState,
    reactionSorting: ReactionSorting,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onThreadClick: (Message) -> Unit,
    onLongItemClick: (Message) -> Unit,
    onReactionsClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit,
    onQuotedMessageClick: (Message) -> Unit,
) {
    MessageContainer(
        messageListItemState = messageListItemState,
        reactionSorting = reactionSorting,
        onLongItemClick = onLongItemClick,
        onReactionsClick = onReactionsClick,
        onThreadClick = onThreadClick,
        onGiphyActionClick = onGiphyActionClick,
        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
        onQuotedMessageClick = onQuotedMessageClick,
    )
}

@Composable
internal fun DefaultMessageListLoadingIndicator(modifier: Modifier) {
    LoadingIndicator(modifier)
}

@Composable
internal fun DefaultMessageListEmptyContent(modifier: Modifier) {
    Box(
        modifier = modifier.background(color = ChatTheme.colors.appBackground),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.stream_compose_message_list_empty_messages),
            style = ChatTheme.typography.body,
            color = ChatTheme.colors.textLowEmphasis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
public fun MessageList(
    currentState: MessageListState,
    threadMessagesStart: ThreadMessagesStart = ThreadMessagesStart.BOTTOM,
    reactionSorting: ReactionSorting,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    messagesLazyListState: MessagesLazyListState =
        rememberMessageListState(parentMessageId = currentState.parentMessageId),
    onMessagesPageStartReached: () -> Unit = {},
    onLastVisibleMessageChanged: (Message) -> Unit = {},
    onScrolledToBottom: () -> Unit = {},
    onThreadClick: (Message) -> Unit = {},
    onLongItemClick: (Message) -> Unit = {},
    onReactionsClick: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onMessagesPageEndReached: (String) -> Unit = {},
    onScrollToBottom: (() -> Unit) -> Unit = {},
    loadingContent: @Composable () -> Unit = { DefaultMessageListLoadingIndicator(modifier) },
    emptyContent: @Composable () -> Unit = { DefaultMessageListEmptyContent(modifier) },
    helperContent: @Composable BoxScope.() -> Unit = {
        DefaultMessagesHelperContent(
            messagesState = currentState,
            messagesLazyListState = messagesLazyListState,
            scrollToBottom = onScrollToBottom,
        )
    },
    loadingMoreContent: @Composable () -> Unit = { DefaultMessagesLoadingMoreIndicator() },
    itemModifier: (index: Int, item: MessageListItemState) -> Modifier = { _, _ ->
        Modifier
    },
    itemContent: @Composable (MessageListItemState) -> Unit = {
        DefaultMessageContainer(
            messageListItemState = it,
            reactionSorting = reactionSorting,
            onLongItemClick = onLongItemClick,
            onThreadClick = onThreadClick,
            onReactionsClick = onReactionsClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    },
) {
    val isLoading = currentState.isLoading
    val messages = currentState.messageItems

    when {
        isLoading -> loadingContent()
        messages.isNotEmpty() -> Messages(
            modifier = modifier,
            contentPadding = contentPadding,
            messagesState = currentState,
            messagesLazyListState = messagesLazyListState,
            onMessagesStartReached = onMessagesPageStartReached,
            threadMessagesStart = threadMessagesStart,
            onLastVisibleMessageChanged = onLastVisibleMessageChanged,
            onScrolledToBottom = onScrolledToBottom,
            helperContent = helperContent,
            loadingMoreContent = loadingMoreContent,
            itemModifier = itemModifier,
            itemContent = itemContent,
            onMessagesEndReached = onMessagesPageEndReached,
            onScrollToBottom = onScrollToBottom,
        )
        else -> emptyContent()
    }
}
package com.example.composechatsample.screen.messages

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.screen.components.LoadingIndicator
import com.example.composechatsample.ui.theme.ThreadMessagesStart
import com.example.composechatsample.screen.HasMessageListItemState
import com.example.composechatsample.screen.MessageItemState
import com.example.composechatsample.screen.MessageListItemState
import com.example.composechatsample.screen.MessageListState
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
@Suppress("LongParameterList", "LongMethod", "ComplexMethod")
public fun Messages(
    messagesState: MessageListState,
    messagesLazyListState: MessagesLazyListState,
    threadMessagesStart: ThreadMessagesStart = ThreadMessagesStart.BOTTOM,
    onMessagesStartReached: () -> Unit,
    onLastVisibleMessageChanged: (Message) -> Unit,
    onScrolledToBottom: () -> Unit,
    onMessagesEndReached: (String) -> Unit,
    onScrollToBottom: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    helperContent: @Composable BoxScope.() -> Unit = {
        DefaultMessagesHelperContent(
            messagesState = messagesState,
            messagesLazyListState = messagesLazyListState,
            scrollToBottom = onScrollToBottom,
        )
    },
    loadingMoreContent: @Composable () -> Unit = { DefaultMessagesLoadingMoreIndicator() },
    itemModifier: (index: Int, item: MessageListItemState) -> Modifier = { _, _ -> Modifier },
    itemContent: @Composable (MessageListItemState) -> Unit,
) {
    val lazyListState = messagesLazyListState.lazyListState
    val messages = messagesState.messageItems
    val endOfMessages = messagesState.endOfOldMessagesReached
    val startOfMessages = messagesState.endOfNewMessagesReached
    val isLoadingMoreNewMessages = messagesState.isLoadingNewerMessages
    val isLoadingMoreOldMessages = messagesState.isLoadingOlderMessages

    val density = LocalDensity.current

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .testTag("Stream_Messages")
                .fillMaxSize()
                .onSizeChanged {
                    val bottomPadding = contentPadding.calculateBottomPadding()
                    val topPadding = contentPadding.calculateTopPadding()

                    val paddingPixels = with(density) {
                        bottomPadding.roundToPx() + topPadding.roundToPx()
                    }

                    val parentSize = IntSize(
                        width = it.width,
                        height = it.height + paddingPixels,
                    )
                    messagesLazyListState.updateParentSize(parentSize)
                },
            state = lazyListState,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = threadMessagesStart.from(messagesState),
            reverseLayout = true,
            contentPadding = contentPadding,
        ) {
            if (isLoadingMoreNewMessages && !startOfMessages) {
                item {
                    loadingMoreContent()
                }
            }

            itemsIndexed(
                messages,
                key = { _, item ->
                    if (item is MessageItemState) item.message.id else item.toString()
                },
            ) { index, item ->
                val messageItemModifier =
                    if (item is MessageItemState && item.focusState == MessageFocused) {
                        Modifier.onSizeChanged {
                            messagesLazyListState.updateFocusedMessageSize(it)
                        }
                    } else {
                        Modifier
                    }

                val itemModifier = itemModifier(index, item)
                val finalItemModifier = messageItemModifier.then(itemModifier)
                Box(modifier = finalItemModifier) {
                    itemContent(item)

                    if (index == 0 && lazyListState.isScrollInProgress) {
                        onScrolledToBottom()
                    }

                    if (!endOfMessages &&
                        index == messages.lastIndex &&
                        messages.isNotEmpty() &&
                        lazyListState.isScrollInProgress
                    ) {
                        onMessagesStartReached()
                    }

                    val newestMessageItem = (messages.firstOrNull { it is MessageItemState } as? MessageItemState)
                    if (index == 0 &&
                        messages.isNotEmpty() &&
                        lazyListState.isScrollInProgress
                    ) {
                        newestMessageItem?.message?.id?.let(onMessagesEndReached)
                    }
                }
            }

            if (isLoadingMoreOldMessages && !endOfMessages) {
                item {
                    loadingMoreContent()
                }
            }
        }

        helperContent()
    }

    OnLastVisibleItemChanged(lazyListState) { messageIndex ->
        val message = messagesState.messageItems.getOrNull(messageIndex)

        if (message is HasMessageListItemState) {
            onLastVisibleMessageChanged(message.message)
        }
    }
}

private fun ThreadMessagesStart.from(messagesState: MessageListState): Arrangement.Vertical =
    when (messagesState.parentMessageId) {
        null -> Arrangement.Top
        else -> when (this) {
            ThreadMessagesStart.BOTTOM -> Arrangement.Bottom
            ThreadMessagesStart.TOP -> Arrangement.Top
        }
    }

@Composable
private fun OnLastVisibleItemChanged(lazyListState: LazyListState, onChanged: (firstVisibleItemIndex: Int) -> Unit) {
    onChanged(lazyListState.firstVisibleItemIndex)
}

@SuppressLint("UnrememberedMutableState")
@Composable
internal fun BoxScope.DefaultMessagesHelperContent(
    messagesState: MessageListState,
    messagesLazyListState: MessagesLazyListState,
    scrollToBottom: (() -> Unit) -> Unit,
) {
    val lazyListState = messagesLazyListState.lazyListState

    val messages = messagesState.messageItems
    val newMessageState = messagesState.newMessageState
    val areNewestMessagesLoaded = messagesState.endOfNewMessagesReached
    val isMessageInThread = messagesState.parentMessageId != null

    val coroutineScope = rememberCoroutineScope()

    val firstVisibleItemIndex = derivedStateOf { lazyListState.firstVisibleItemIndex }

    val focusedItemIndex = messages.indexOfFirst { it is MessageItemState && it.focusState is MessageFocused }

    val offset = messagesLazyListState.focusedMessageOffset

    LaunchedEffect(newMessageState, focusedItemIndex, offset) {
        if (focusedItemIndex != -1 &&
            !lazyListState.isScrollInProgress
        ) {
            coroutineScope.launch {
                lazyListState.scrollToItem(focusedItemIndex, offset)
            }
        }

        val shouldScrollToBottom = shouldScrollToBottom(
            focusedItemIndex,
            firstVisibleItemIndex.value,
            newMessageState,
            areNewestMessagesLoaded,
            lazyListState.isScrollInProgress,
        )

        if (shouldScrollToBottom) {
            coroutineScope.launch {
                if (newMessageState is MyOwn && firstVisibleItemIndex.value > 5) {
                    lazyListState.scrollToItem(5)
                }
                lazyListState.animateScrollToItem(0)
            }
        }
    }

    if (isScrollToBottomButtonVisible(isMessageInThread, firstVisibleItemIndex.value, areNewestMessagesLoaded)) {
        MessagesScrollingOption(
            unreadCount = messagesState.unreadCount,
            modifier = Modifier.align(Alignment.BottomEnd),
            onClick = {
                scrollToBottom {
                    coroutineScope.launch {
                        if (firstVisibleItemIndex.value > 5) {
                            lazyListState.scrollToItem(5)
                        }
                        lazyListState.animateScrollToItem(0)
                    }
                }
            },
        )
    }
}

private fun shouldScrollToBottom(
    focusedItemIndex: Int,
    firstVisibleItemIndex: Int,
    newMessageState: NewMessageState?,
    areNewestMessagesLoaded: Boolean,
    isScrollInProgress: Boolean,
): Boolean {
    newMessageState ?: return false

    return focusedItemIndex == -1 &&
        !isScrollInProgress &&
        areNewestMessagesLoaded &&
        firstVisibleItemIndex < 3
}

private fun isScrollToBottomButtonVisible(
    isInThread: Boolean,
    firstVisibleItemIndex: Int,
    areNewestMessagesLoaded: Boolean,
): Boolean {
    return if (isInThread) {
        isScrollToBottomButtonVisibleInThread(firstVisibleItemIndex)
    } else {
        isScrollToBottomButtonVisibleInMessageList(firstVisibleItemIndex, areNewestMessagesLoaded)
    }
}

private fun isScrollToBottomButtonVisibleInThread(firstVisibleItemIndex: Int): Boolean {
    return shouldScrollToBottomButtonBeVisibleAtIndex(firstVisibleItemIndex)
}

private fun isScrollToBottomButtonVisibleInMessageList(
    firstVisibleItemIndex: Int,
    areNewestMessagesLoaded: Boolean,
): Boolean {
    return shouldScrollToBottomButtonBeVisibleAtIndex(firstVisibleItemIndex) || !areNewestMessagesLoaded
}

private fun shouldScrollToBottomButtonBeVisibleAtIndex(firstVisibleItemIndex: Int): Boolean {
    return abs(firstVisibleItemIndex) >= 3
}

@Composable
internal fun DefaultMessagesLoadingMoreIndicator() {
    LoadingIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
    )
}
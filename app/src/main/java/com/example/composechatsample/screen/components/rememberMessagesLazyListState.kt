package com.example.composechatsample.screen.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.composechatsample.screen.messages.MessagesLazyListState

@Composable
public fun rememberMessagesLazyListState(
    initialFirstVisibleItemIndex: Int = 0,
    initialFirstVisibleItemScrollOffset: Int = 0,
    messageOffsetHandler: MessagesLazyListState.MessageOffsetHandler = MessagesLazyListState.defaultOffsetHandler,
): MessagesLazyListState {
    return rememberSaveable(saver = MessagesLazyListState.Saver) {
        MessagesLazyListState(
            lazyListState = LazyListState(
                firstVisibleItemIndex = initialFirstVisibleItemIndex,
                firstVisibleItemScrollOffset = initialFirstVisibleItemScrollOffset,
            ),
            messageOffsetHandler = messageOffsetHandler,
        )
    }
}

@Composable
public fun rememberMessageListState(
    initialFirstVisibleItemIndex: Int = 0,
    initialFirstVisibleItemScrollOffset: Int = 0,
    parentMessageId: String? = null,
    messageOffsetHandler: MessagesLazyListState.MessageOffsetHandler = MessagesLazyListState.defaultOffsetHandler,
): MessagesLazyListState {
    val baseListState = rememberMessagesLazyListState(
        initialFirstVisibleItemIndex = initialFirstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = initialFirstVisibleItemScrollOffset,
        messageOffsetHandler = messageOffsetHandler,
    )

    return if (parentMessageId != null) {
        rememberMessagesLazyListState(messageOffsetHandler = messageOffsetHandler)
    } else {
        baseListState
    }
}
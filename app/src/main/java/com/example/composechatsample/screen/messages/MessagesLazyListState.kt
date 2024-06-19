package com.example.composechatsample.screen.messages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.ui.unit.IntSize
import java.io.Serializable

public data class MessagesLazyListState(
    public val lazyListState: LazyListState,
    private val messageOffsetHandler: MessageOffsetHandler = defaultOffsetHandler,
) {


    private val _parentSize: MutableState<IntSize> = mutableStateOf(IntSize.Zero)

    private val _focusedMessageSize: MutableState<IntSize> = mutableStateOf(IntSize.Zero)

    private val _focusedMessageOffset: MutableState<Int> = mutableStateOf(0)
    public val focusedMessageOffset: Int by _focusedMessageOffset

    public fun updateParentSize(parentSize: IntSize) {
        if (parentSize == _parentSize.value) return
        _parentSize.value = parentSize
        calculateMessageOffset(parentSize, _focusedMessageSize.value)
    }

    public fun updateFocusedMessageSize(focusedMessageSize: IntSize) {
        if (focusedMessageSize == _focusedMessageSize.value) return
        _focusedMessageSize.value = focusedMessageSize
        calculateMessageOffset(_parentSize.value, focusedMessageSize)
    }

    private fun calculateMessageOffset(parentSize: IntSize, focusedMessageSize: IntSize) {
        _focusedMessageOffset.value = messageOffsetHandler.calculateOffset(parentSize, focusedMessageSize)
    }

    public fun interface MessageOffsetHandler : Serializable {
        public fun calculateOffset(parentSize: IntSize, focusedMessageSize: IntSize): Int
    }

    public companion object {
        private const val KeyFirstVisibleItemIndex: String = "firstVisibleItemIndex"
        private const val KeyFirstVisibleItemScrollOffset: String = "firstVisibleItemScrollOffset"
        private const val KeyMessageOffsetHandler: String = "messageOffsetHandler"

        public val Saver: Saver<MessagesLazyListState, *> = mapSaver(
            save = {
                mapOf(
                    KeyFirstVisibleItemIndex to it.lazyListState.firstVisibleItemIndex,
                    KeyFirstVisibleItemScrollOffset to it.lazyListState.firstVisibleItemScrollOffset,
                    KeyMessageOffsetHandler to it.messageOffsetHandler,
                )
            },
            restore = {
                MessagesLazyListState(
                    LazyListState(
                        firstVisibleItemIndex = (it[KeyFirstVisibleItemIndex] as? Int) ?: 0,
                        firstVisibleItemScrollOffset = (it[KeyFirstVisibleItemScrollOffset] as? Int) ?: 0,
                    ),
                    messageOffsetHandler = (it[KeyMessageOffsetHandler] as? MessageOffsetHandler)
                        ?: defaultOffsetHandler,
                )
            },
        )

        internal val defaultOffsetHandler: MessageOffsetHandler =
            MessageOffsetHandler { parentSize, focusedMessageSize ->
                when {
                    parentSize.height == 0 && focusedMessageSize.height == 0 -> 0
                    parentSize.height != 0 && focusedMessageSize.height == 0 -> -parentSize.height / 2
                    else -> {
                        -parentSize.height / 2
                        val sizeDiff = parentSize.height - focusedMessageSize.height
                        if (sizeDiff > 0) {
                            -sizeDiff / 2
                        } else {
                            -sizeDiff
                        }
                    }
                }
            }
    }
}
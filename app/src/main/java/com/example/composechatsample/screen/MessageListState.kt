package com.example.composechatsample.screen

import com.example.composechatsample.core.models.User
import com.example.composechatsample.screen.messages.NewMessageState
import com.example.composechatsample.screen.messages.SelectedMessageState

data class MessageListState(
    val messageItems: List<MessageListItemState> = emptyList(),
    val endOfNewMessagesReached: Boolean = true,
    val endOfOldMessagesReached: Boolean = false,
    public val isLoading: Boolean = false,
    public val isLoadingNewerMessages: Boolean = false,
    public val isLoadingOlderMessages: Boolean = false,
    public val currentUser: User? = User(),
    public val parentMessageId: String? = null,
    public val unreadCount: Int = 0,
    public val newMessageState: NewMessageState? = null,
    public val selectedMessageState: SelectedMessageState? = null,
)

internal fun MessageListState.stringify(): String {
    return "MessageListState(" +
        "messageItems.size: ${messageItems.size}, " +
        "endOfNewMessagesReached: $endOfNewMessagesReached, " +
        "endOfOldMessagesReached: $endOfOldMessagesReached, " +
        "isLoading: $isLoading, " +
        "isLoadingNewerMessages: $isLoadingNewerMessages, " +
        "isLoadingOlderMessages: $isLoadingOlderMessages, " +
        "currentUser.id: ${currentUser?.id}, " +
        "parentMessageId: $parentMessageId, " +
        "unreadCount: $unreadCount, " +
        "newMessageState: $newMessageState, " +
        "selectedMessageState: $selectedMessageState)"
}
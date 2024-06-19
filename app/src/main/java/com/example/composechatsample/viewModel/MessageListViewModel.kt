package com.example.composechatsample.viewModel

import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composechatsample.DeletedMessageVisibility
import com.example.composechatsample.core.asState
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.ConnectionState
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.log.taggedLogger
import com.example.composechatsample.screen.GiphyAction
import com.example.composechatsample.screen.MessageListState
import com.example.composechatsample.screen.messages.MessageAction
import com.example.composechatsample.screen.messages.MessageMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

public class MessageListViewModel(
    internal val messageListController: MessageListController,
) : ViewModel() {

    private val logger by taggedLogger("Chat:MessageListVM")

    public val currentMessagesState: MessageListState
        get() = if (isInThread) threadMessagesState else messagesState

    private val messagesState: MessageListState by messageListController.messageListState
        .map { it.copy(messageItems = it.messageItems.reversed()) }
        .asState(viewModelScope, MessageListState())

    private val threadMessagesState: MessageListState by messageListController.threadListState
        .map { it.copy(messageItems = it.messageItems.reversed()) }
        .asState(viewModelScope, MessageListState())

    public val messageMode: MessageMode by messageListController.mode.asState(viewModelScope)

    public val channel: Channel by messageListController.channel.asState(viewModelScope)

    public val typingUsers: List<User> by messageListController.typingUsers.asState(viewModelScope)

    public val messageActions: Set<MessageAction> by messageListController.messageActions.asState(viewModelScope)

    public val isInThread: Boolean
        get() = messageListController.isInThread

    public val isShowingOverlay: Boolean
        get() = currentMessagesState.selectedMessageState != null

    public val connectionState: StateFlow<ConnectionState> = messageListController.connectionState

    public val isOnline: Flow<Boolean> = messageListController.connectionState.map { it is ConnectionState.Connected }

    public val user: StateFlow<User?> = messageListController.user

    public val showSystemMessagesState: Boolean by messageListController.showSystemMessagesState.asState(viewModelScope)

    public val messageFooterVisibilityState: MessageFooterVisibility by messageListController
        .messageFooterVisibilityState.asState(viewModelScope)

    public val deletedMessageVisibilityState: DeletedMessageVisibility by messageListController
        .deletedMessageVisibilityState.asState(viewModelScope)

    public fun updateLastSeenMessage(message: Message) {
        messageListController.updateLastSeenMessage(message)
    }

    internal fun onBottomEndRegionReached(
        baseMessageId: String,
        messageLimit: Int = messageListController.messageLimit,
    ) {
        logger.i { "[onBottomEndRegionReached] baseMessageId: $baseMessageId, messageLimit: $messageLimit" }
        loadNewerMessages(baseMessageId, messageLimit)
    }

    public fun loadNewerMessages(messageId: String, messageLimit: Int = messageListController.messageLimit) {
        messageListController.loadNewerMessages(messageId, messageLimit)
    }

    public fun loadOlderMessages(messageLimit: Int = messageListController.messageLimit) {
        messageListController.loadOlderMessages(messageLimit)
    }

    public fun selectMessage(message: Message?) {
        messageListController.selectMessage(message)
    }

    public fun selectReactions(message: Message?) {
        messageListController.selectReactions(message)
    }

    public fun selectExtendedReactions(message: Message?) {
        messageListController.selectExtendedReactions(message)
    }

    public fun openMessageThread(message: Message) {
        viewModelScope.launch {
            messageListController.enterThreadMode(message)
        }
    }

    public fun dismissMessageAction(messageAction: MessageAction) {
        messageListController.dismissMessageAction(messageAction)
    }

    public fun dismissAllMessageActions() {
        messageListController.dismissAllMessageActions()
    }

    public fun performMessageAction(messageAction: MessageAction) {
        viewModelScope.launch {
            messageListController.performMessageAction(messageAction)
        }
    }

    @JvmOverloads
    @Suppress("ConvertArgumentToSet")
    public fun deleteMessage(message: Message, hard: Boolean = false) {
        messageListController.deleteMessage(message, hard)
    }

    @Suppress("ConvertArgumentToSet")
    public fun flagMessage(
        message: Message,
        reason: String?,
        customData: Map<String, String>,
    ) {
        messageListController.flagMessage(
            message,
            reason,
            customData,
        )
    }

    public fun muteUser(userId: String, timeout: Int? = null) {
        messageListController.muteUser(userId, timeout)
    }

    public fun unmuteUser(userId: String) {
        messageListController.unmuteUser(userId)
    }

    public fun banUser(
        userId: String,
        reason: String? = null,
        timeout: Int? = null,
    ) {
        messageListController.banUser(userId = userId, reason = reason, timeout = timeout)
    }

    public fun unbanUser(userId: String) {
        messageListController.unbanUser(userId)
    }

    public fun shadowBanUser(
        userId: String,
        reason: String? = null,
        timeout: Int? = null,
    ) {
        messageListController.shadowBanUser(userId = userId, reason = reason, timeout = timeout)
    }

    public fun removeShadowBanFromUser(userId: String) {
        messageListController.removeShadowBanFromUser(userId)
    }

    public fun leaveThread() {
        messageListController.enterNormalMode()
    }

    public fun removeOverlay() {
        messageListController.removeOverlay()
    }

    public fun clearNewMessageState() {
        messageListController.clearNewMessageState()
    }

    public fun getMessageById(messageId: String): Message? {
        return messageListController.getMessageFromListStateById(messageId)
    }

    public fun performGiphyAction(action: GiphyAction) {
        messageListController.performGiphyAction(action)
    }

    public fun scrollToMessage(
        messageId: String,
        parentMessageId: String?,
    ) {
        messageListController.scrollToMessage(
            messageId = messageId,
            parentMessageId = parentMessageId,
        )
    }

    public fun scrollToBottom(messageLimit: Int = messageListController.messageLimit, scrollToBottom: () -> Unit) {
        messageListController.scrollToBottom(messageLimit, scrollToBottom)
    }

    public fun setMessagePositionHandler(messagePositionHandler: MessagePositionHandler) {
        messageListController.setMessagePositionHandler(messagePositionHandler)
    }

    public fun setDateSeparatorHandler(dateSeparatorHandler: DateSeparatorHandler?) {
        messageListController.setDateSeparatorHandler(dateSeparatorHandler)
    }

    public fun setThreadDateSeparatorHandler(threadDateSeparatorHandler: DateSeparatorHandler?) {
        messageListController.setThreadDateSeparatorHandler(threadDateSeparatorHandler)
    }

    public fun setMessageFooterVisibility(messageFooterVisibility: MessageFooterVisibility) {
        messageListController.setMessageFooterVisibility(messageFooterVisibility)
    }

    public fun setDeletedMessageVisibility(deletedMessageVisibility: DeletedMessageVisibility) {
        messageListController.setDeletedMessageVisibility(deletedMessageVisibility)
    }

    public fun setSystemMessageVisibility(areSystemMessagesVisible: Boolean) {
        messageListController.setSystemMessageVisibility(areSystemMessagesVisible)
    }

    override fun onCleared() {
        messageListController.onCleared()
        super.onCleared()
    }
}
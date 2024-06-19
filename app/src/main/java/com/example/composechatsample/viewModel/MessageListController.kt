package com.example.composechatsample.viewModel

import androidx.annotation.VisibleForTesting
import com.example.composechatsample.DeletedMessageVisibility
import com.example.composechatsample.common.ClipboardHandler
import com.example.composechatsample.core.ChannelState
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.Debouncer
import com.example.composechatsample.core.DispatcherProvider
import com.example.composechatsample.core.cidToTypeAndId
import com.example.composechatsample.core.diff
import com.example.composechatsample.core.enqueue
import com.example.composechatsample.core.extractCause
import com.example.composechatsample.core.getCreatedAtOrDefault
import com.example.composechatsample.core.getCreatedAtOrNull
import com.example.composechatsample.core.isDeleted
import com.example.composechatsample.core.isError
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.isGiphy
import com.example.composechatsample.core.isModerationBounce
import com.example.composechatsample.core.isModerationError
import com.example.composechatsample.core.isSystem
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.ChannelUserRead
import com.example.composechatsample.core.models.ConnectionState
import com.example.composechatsample.core.models.Flag
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.MessagesState
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.state.ClientState
import com.example.composechatsample.core.wasCreatedAfter
import com.example.composechatsample.log.TaggedLogger
import com.example.composechatsample.log.taggedLogger
import com.example.composechatsample.screen.CancelGiphy
import com.example.composechatsample.screen.DateSeparatorItemState
import com.example.composechatsample.screen.EmptyThreadPlaceholderItemState
import com.example.composechatsample.screen.GiphyAction
import com.example.composechatsample.screen.HasMessageListItemState
import com.example.composechatsample.screen.MessageItemState
import com.example.composechatsample.screen.MessageListItemState
import com.example.composechatsample.screen.MessageListState
import com.example.composechatsample.screen.SendGiphy
import com.example.composechatsample.screen.ShuffleGiphy
import com.example.composechatsample.screen.StartOfTheChannelItemState
import com.example.composechatsample.screen.SystemMessageItemState
import com.example.composechatsample.screen.ThreadDateSeparatorItemState
import com.example.composechatsample.screen.TypingItemState
import com.example.composechatsample.screen.UnreadSeparatorItemState
import com.example.composechatsample.screen.messages.Copy
import com.example.composechatsample.screen.messages.Delete
import com.example.composechatsample.screen.messages.MarkAsUnread
import com.example.composechatsample.screen.messages.MessageAction
import com.example.composechatsample.screen.messages.MessageFocusRemoved
import com.example.composechatsample.screen.messages.MessageFocused
import com.example.composechatsample.screen.messages.MessageMode
import com.example.composechatsample.screen.messages.MessagePosition
import com.example.composechatsample.screen.messages.MyOwn
import com.example.composechatsample.screen.messages.NewMessageState
import com.example.composechatsample.screen.messages.Other
import com.example.composechatsample.screen.messages.Pin
import com.example.composechatsample.screen.messages.React
import com.example.composechatsample.screen.messages.Reply
import com.example.composechatsample.screen.messages.Resend
import com.example.composechatsample.screen.messages.SelectedMessageFailedModerationState
import com.example.composechatsample.screen.messages.SelectedMessageOptionsState
import com.example.composechatsample.screen.messages.SelectedMessageReactionsPickerState
import com.example.composechatsample.screen.messages.SelectedMessageReactionsState
import com.example.composechatsample.screen.messages.SelectedMessageState
import com.example.composechatsample.screen.messages.ThreadReply
import com.example.composechatsample.screen.stringify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

public class MessageListController(
    private val cid: String,
    private val clipboardHandler: ClipboardHandler,
    public val threadLoadOrderOlderToNewer: Boolean,
    private val messageId: String? = null,
    private val parentMessageId: String? = null,
    public val messageLimit: Int = DEFAULT_MESSAGES_LIMIT,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val clientState: ClientState = chatClient.clientState,
    private val deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
    private val showSystemMessages: Boolean = true,
    private val messageFooterVisibility: MessageFooterVisibility = MessageFooterVisibility.WithTimeDifference(),
    private val enforceUniqueReactions: Boolean = true,
    private val dateSeparatorHandler: DateSeparatorHandler = DateSeparatorHandler.getDefaultDateSeparatorHandler(),
    private val threadDateSeparatorHandler: DateSeparatorHandler =
        DateSeparatorHandler.getDefaultThreadDateSeparatorHandler(),
    private val messagePositionHandler: MessagePositionHandler = MessagePositionHandler.defaultHandler(),
    private val showDateSeparatorInEmptyThread: Boolean = false,
    private val showThreadSeparatorInEmptyThread: Boolean = false,
) {


    private val logger: TaggedLogger by taggedLogger("MessageListController")

    private val scope = CoroutineScope(DispatcherProvider.Immediate)

    public val channelState: StateFlow<ChannelState?> = observeChannelState()

    public val connectionState: StateFlow<ConnectionState> = clientState.connectionState

    public val user: StateFlow<User?> = clientState.user

    public val unreadLabelState: MutableStateFlow<UnreadLabel?> = MutableStateFlow(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    public val ownCapabilities: StateFlow<Set<String>> = channelState.filterNotNull()
        .flatMapLatest { it.channelData }
        .map { it.ownCapabilities }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = setOf())

    @OptIn(ExperimentalCoroutinesApi::class)
    public val channel: StateFlow<Channel> = channelState.filterNotNull()
        .flatMapLatest { state ->
            combine(
                state.channelData,
                state.membersCount,
                state.watcherCount,
            ) { _, _, _ ->
                state.toChannel()
            }
        }
        .onEach { channel ->
            chatClient.dismissChannelNotifications(
                channelType = channel.type,
                channelId = channel.id,
            )
        }
        .distinctUntilChanged()
        .stateIn(scope = scope, started = SharingStarted.Eagerly, Channel())

    private val _mode: MutableStateFlow<MessageMode> = MutableStateFlow(MessageMode.Normal)
    public val mode: StateFlow<MessageMode> = _mode

    public val isInThread: Boolean
        get() = _mode.value is MessageMode.MessageThread

    private val _errorEvents: MutableStateFlow<ErrorEvent?> = MutableStateFlow(null)
    public val errorEvents: StateFlow<ErrorEvent?> = _errorEvents

    public val unreadCount: StateFlow<Int> = channelState.filterNotNull()
        .flatMapLatest { it.unreadCount }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = 0)

    public val typingUsers: StateFlow<List<User>> = channelState.filterNotNull()
        .flatMapLatest { it.typing }
        .map { it.users }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = emptyList())

    private val _messageListState: MutableStateFlow<MessageListState> =
        MutableStateFlow(MessageListState(isLoading = true))
    public val messageListState: StateFlow<MessageListState> = _messageListState

    private val _threadListState: MutableStateFlow<MessageListState> =
        MutableStateFlow(MessageListState(isLoading = true))
    public val threadListState: StateFlow<MessageListState> = _threadListState

    public val listState: StateFlow<MessageListState> = _mode.flatMapLatest {
        if (it is MessageMode.MessageThread) {
            _threadListState
        } else {
            _messageListState
        }
    }.stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = MessageListState(isLoading = true))

    private val messagesState: MessageListState
        get() = if (isInThread) _threadListState.value else _messageListState.value

    private var lastLoadedMessage: Message? = null

    private var lastLoadedThreadMessage: Message? = null

    private val _messageActions: MutableStateFlow<Set<MessageAction>> = MutableStateFlow(emptySet())
    public val messageActions: StateFlow<Set<MessageAction>> = _messageActions

    private var _messagePositionHandler: MutableStateFlow<MessagePositionHandler> =
        MutableStateFlow(messagePositionHandler)

    private val _dateSeparatorHandler: MutableStateFlow<DateSeparatorHandler> = MutableStateFlow(dateSeparatorHandler)

    private val _threadDateSeparatorHandler: MutableStateFlow<DateSeparatorHandler> =
        MutableStateFlow(threadDateSeparatorHandler)

    private val _showSystemMessagesState: MutableStateFlow<Boolean> = MutableStateFlow(showSystemMessages)
    public val showSystemMessagesState: StateFlow<Boolean> = _showSystemMessagesState

    private val _messageFooterVisibilityState: MutableStateFlow<MessageFooterVisibility> =
        MutableStateFlow(messageFooterVisibility)
    public val messageFooterVisibilityState: StateFlow<MessageFooterVisibility> = _messageFooterVisibilityState

    private val _deletedMessageVisibilityState: MutableStateFlow<DeletedMessageVisibility> =
        MutableStateFlow(deletedMessageVisibility)
    public val deletedMessageVisibilityState: StateFlow<DeletedMessageVisibility> = _deletedMessageVisibilityState


    private var focusedMessage: MutableStateFlow<Message?> = MutableStateFlow(null)

    private var removeFocusedMessageJob: Pair<String, Job>? = null

    public val isInsideSearch: StateFlow<Boolean> = channelState.filterNotNull()
        .flatMapLatest { it.insideSearch }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = false)

    private var threadJob: Job? = null

    private val debouncer = Debouncer(debounceMs = 200L, scope = scope)

    @Volatile
    private var lastSeenChannelMessageId: String? = null

    @Volatile
    private var lastSeenThreadMessageId: String? = null

    @VisibleForTesting
    internal var lastSeenMessageId: String?
        get() = if (isInThread) lastSeenThreadMessageId else lastSeenChannelMessageId
        set(value) = if (isInThread) {
            lastSeenThreadMessageId = value
        } else {
            lastSeenChannelMessageId = value
        }

    init {
        observeMessagesListState()
        processMessageId()
    }

    private fun observeChannelState(): StateFlow<ChannelState?> {
        logger.d { "[observeChannelState] cid: $cid, messageId: $messageId, messageLimit: $messageLimit" }
        return chatClient.watchChannelAsState(
            cid = cid,
            messageLimit = messageLimit,
            coroutineScope = scope,
        )
    }

    @Suppress("MagicNumber")
    private fun observeMessagesListState() {
        channelState.filterNotNull().flatMapLatest { channelState ->
            val channel = channelState.toChannel()
            combine(
                channelState.messagesState,
                channelState.reads,
                _showSystemMessagesState,
                _dateSeparatorHandler,
                _deletedMessageVisibilityState,
                _messageFooterVisibilityState,
                _messagePositionHandler,
                typingUsers,
                focusedMessage,
                channelState.endOfNewerMessages,
                unreadLabelState,
                channelState.members,
                channelState.endOfOlderMessages,
            ) { data ->
                val state = data[0] as MessagesState
                val reads = data[1] as List<ChannelUserRead>
                val showSystemMessages = data[2] as Boolean
                val dateSeparatorHandler = data[3] as DateSeparatorHandler
                val deletedMessageVisibility = data[4] as DeletedMessageVisibility
                val messageFooterVisibility = data[5] as MessageFooterVisibility
                val messagePositionHandler = data[6] as MessagePositionHandler
                val typingUsers = data[7] as List<User>
                val focusedMessage = data[8] as Message?
                val endOfNewerMessages = data[9] as Boolean
                val unreadLabel = data[10] as UnreadLabel?
                val members = data[11] as List<Member>
                val endOfOlderMessages = data[12] as Boolean

                when (state) {
                    is MessagesState.Loading,
                    is MessagesState.NoQueryActive,
                    -> _messageListState.value.copy(isLoading = true)
                    is MessagesState.OfflineNoResults -> _messageListState.value.copy(isLoading = false)
                    is MessagesState.Result -> _messageListState.value.copy(
                        isLoading = false,
                        messageItems = groupMessages(
                            messages = filterMessagesToShow(
                                messages = state.messages,
                                showSystemMessages = showSystemMessages,
                                deletedMessageVisibility = deletedMessageVisibility,
                            ),
                            isInThread = false,
                            reads = reads,
                            dateSeparatorHandler = dateSeparatorHandler,
                            deletedMessageVisibility = deletedMessageVisibility,
                            messageFooterVisibility = messageFooterVisibility,
                            messagePositionHandler = messagePositionHandler,
                            typingUsers = typingUsers,
                            focusedMessage = focusedMessage,
                            unreadLabel = unreadLabel,
                            members = members,
                            endOfOlderMessages = endOfOlderMessages,
                            channel = channel.copy(
                                members = members,
                                read = reads,
                            ),
                        ),
                        endOfNewMessagesReached = endOfNewerMessages,
                    )
                }
            }.distinctUntilChanged()
        }.catch {
            it.cause?.printStackTrace()
            showEmptyState()
        }.onEach { newState ->
            updateMessageList(newState)
        }.launchIn(scope)

        channelState.filterNotNull().flatMapLatest { it.endOfOlderMessages }.onEach {
            updateEndOfOldMessagesReached(it)
        }.launchIn(scope)

        user.onEach {
            updateCurrentUser(it)
        }.launchIn(scope)

        unreadCount.onEach {
            updateUnreadCount(it)
        }.launchIn(scope)

        channelState.filterNotNull().flatMapLatest { it.loadingOlderMessages }.onEach {
            updateIsLoadingOlderMessages(it)
        }.launchIn(scope)

        channelState.filterNotNull().flatMapLatest { it.loadingNewerMessages }.onEach {
            updateIsLoadingNewerMessages(it)
        }.launchIn(scope)
        refreshUnreadLabel(true)
    }

    private fun refreshUnreadLabel(shouldShowButton: Boolean) {
        val previousUnreadMessageId = unreadLabelState.value?.lastReadMessageId
        channelState.filterNotNull()
            .flatMapLatest {
                it.read
                    .filterNotNull()
                    .filter {
                        it.lastReadMessageId != null &&
                            previousUnreadMessageId?.equals(it.lastReadMessageId)?.not() ?: true
                    }
            }
            .onFirst { channelUserRead ->
                unreadLabelState.value = channelUserRead.lastReadMessageId
                    ?.takeUnless { channelState.value?.messages?.value?.lastOrNull()?.id == it }
                    ?.let {
                        UnreadLabel(channelUserRead.unreadMessages, it, shouldShowButton)
                    }
            }.launchIn(scope)
    }

    public fun disableUnreadLabelButton() {
        unreadLabelState.value = unreadLabelState.value?.copy(buttonVisibility = false)
    }

    public fun scrollToFirstUnreadMessage() {
        unreadLabelState.value?.let { unreadLabel ->
            val messages = messagesState.messageItems
                .filterIsInstance<MessageItemState>()
                .map { it.message }

            messages.firstOrNull { it.id == unreadLabel.lastReadMessageId }
                ?.let { messages.focusUnreadMessage(it.id) }
                ?: {
                    scope.launch {
                        chatClient.loadMessagesAroundId(cid, unreadLabel.lastReadMessageId)
                            .await()
                            .onSuccess { channel -> channel.messages.focusUnreadMessage(unreadLabel.lastReadMessageId) }
                    }
                }
        }
        disableUnreadLabelButton()
    }

    private fun processMessageId() {
        messageId
            ?.takeUnless { it.isBlank() }
            ?.let { messageId ->
                logger.i { "[processMessageId] messageId: $messageId, parentMessageId: $parentMessageId" }
                scope.launch {
                    if (parentMessageId != null) {
                        enterThreadSequential(parentMessageId)
                    }
                    listState
                        .onCompletion {
                            logger.v { "[processMessageId] mode: ${_mode.value}" }
                            when {
                                _mode.value is MessageMode.Normal -> focusChannelMessage(messageId)
                                _mode.value is MessageMode.MessageThread && parentMessageId != null ->
                                    focusThreadMessage(
                                        threadMessageId = messageId,
                                        parentMessageId = parentMessageId,
                                    )
                            }
                        }
                        .first { it.messageItems.isNotEmpty() }
                }
            }
    }

    private fun List<Message>.focusUnreadMessage(lastReadMessageId: String) {
        indexOfFirst { it.id == lastReadMessageId }
            .takeIf { it != -1 }
            ?.takeUnless { it >= size - 1 }
            ?.let { focusChannelMessage(get(it + 1).id) }
    }

    private fun updateMessageList(newState: MessageListState) {
        if (_messageListState.value.messageItems.isEmpty() &&
            !newState.endOfNewMessagesReached &&
            messageId == null
        ) {
            logger.w { "[updateMessageList] #messageList; rejected (N1)" }
            return
        }
        val first = newState.messageItems.filterIsInstance<MessageItemState>().firstOrNull()?.stringify()
        val last = newState.messageItems.filterIsInstance<MessageItemState>().lastOrNull()?.stringify()
        logger.d { "[updateMessageList] #messageList; first: $first, last: $last" }

        val newLastMessage =
            newState.messageItems.lastOrNull { it is MessageItemState || it is SystemMessageItemState }?.let {
                when (it) {
                    is MessageItemState -> it.message
                    is SystemMessageItemState -> it.message
                    else -> null
                }
            }

        val newMessageState = getNewMessageState(newLastMessage, lastLoadedMessage)
        setMessageListState(newState.copy(newMessageState = newMessageState))
        if (newMessageState != null) lastLoadedMessage = newLastMessage
    }

    private fun updateEndOfOldMessagesReached(endOfOldMessagesReached: Boolean) {
        logger.d { "[updateEndOfOldMessagesReached] #messageList; endOfOldMessagesReached: $endOfOldMessagesReached" }
        setMessageListState(_messageListState.value.copy(endOfOldMessagesReached = endOfOldMessagesReached))
    }

    private fun updateCurrentUser(currentUser: User?) {
        logger.d { "[updateCurrentUser] #messageList; currentUser.id: ${currentUser?.id}" }
        setMessageListState(_messageListState.value.copy(currentUser = currentUser))
    }

    private fun updateUnreadCount(unreadCount: Int) {
        logger.d { "[updateUnreadCount] #messageList; unreadCount: $unreadCount" }
        setMessageListState(_messageListState.value.copy(unreadCount = unreadCount))
    }

    private fun updateIsLoadingOlderMessages(isLoadingOlderMessages: Boolean) {
        logger.d { "[updateIsLoadingOlderMessages] #messageList; isLoadingOlderMessages: $isLoadingOlderMessages" }
        setMessageListState(_messageListState.value.copy(isLoadingOlderMessages = isLoadingOlderMessages))
    }

    private fun updateIsLoadingNewerMessages(isLoadingNewerMessages: Boolean) {
        logger.d { "[updateIsLoadingNewerMessages] #messageList; isLoadingNewerMessages: $isLoadingNewerMessages" }
        setMessageListState(_messageListState.value.copy(isLoadingNewerMessages = isLoadingNewerMessages))
    }

    private fun setMessageListState(newState: MessageListState) {
        logger.v { "[setMessageListState] #messageList; newState: ${newState.stringify()}" }
        _messageListState.value = newState
    }

    @Suppress("MagicNumber", "LongMethod")
    private fun observeThreadMessagesState(
        threadId: String,
        messages: StateFlow<List<Message>>,
        endOfOlderMessages: StateFlow<Boolean>,
        reads: StateFlow<List<ChannelUserRead>>,
        members: StateFlow<List<Member>>,
    ) {
        threadJob = scope.launch {
            user.onEach {
                _threadListState.value = _threadListState.value.copy(currentUser = it)
            }.launchIn(this)

            endOfOlderMessages.onEach {
                _threadListState.value = _threadListState.value.copy(
                    endOfOldMessagesReached = it,
                    isLoadingOlderMessages = when {
                        it -> false
                        else -> _threadListState.value.isLoadingOlderMessages
                    },
                )
            }.launchIn(this)

            combine(
                messages,
                reads,
                _showSystemMessagesState,
                _threadDateSeparatorHandler,
                _deletedMessageVisibilityState,
                _messageFooterVisibilityState,
                _messagePositionHandler,
                typingUsers,
                focusedMessage,
                members,
            ) { data ->
                val messages = data[0] as List<Message>
                val reads = data[1] as List<ChannelUserRead>
                val showSystemMessages = data[2] as Boolean
                val dateSeparatorHandler = data[3] as DateSeparatorHandler
                val deletedMessageVisibility = data[4] as DeletedMessageVisibility
                val messageFooterVisibility = data[5] as MessageFooterVisibility
                val messagePositionHandler = data[6] as MessagePositionHandler
                val typingUsers = data[7] as List<User>
                val focusedMessage = data[8] as Message?
                val members = data[9] as List<Member>

                _threadListState.value.copy(
                    isLoading = false,
                    messageItems = groupMessages(
                        messages = filterMessagesToShow(
                            messages = messages,
                            showSystemMessages = showSystemMessages,
                            deletedMessageVisibility = deletedMessageVisibility,
                        ),
                        isInThread = true,
                        reads = reads,
                        deletedMessageVisibility = deletedMessageVisibility,
                        dateSeparatorHandler = dateSeparatorHandler,
                        messageFooterVisibility = messageFooterVisibility,
                        messagePositionHandler = messagePositionHandler,
                        typingUsers = typingUsers,
                        focusedMessage = focusedMessage,
                        unreadLabel = null,
                        members = members,
                        endOfOlderMessages = false,
                        channel = null,
                    ),
                    parentMessageId = threadId,
                    endOfNewMessagesReached = true,
                )
            }.onFirst {
                lastLoadedThreadMessage =
                    (it.messageItems.lastOrNull { it is MessageItemState } as? MessageItemState)?.message
            }.collect { newState ->
                val newLastMessage =
                    (newState.messageItems.lastOrNull { it is MessageItemState } as? MessageItemState)?.message

                val newMessageState = getNewMessageState(newLastMessage, lastLoadedThreadMessage)

                _threadListState.value = newState.copy(newMessageState = newMessageState)
                if (newMessageState != null) lastLoadedThreadMessage = newLastMessage
            }
        }
    }

    @Suppress("LongParameterList", "LongMethod")
    private fun groupMessages(
        messages: List<Message>,
        isInThread: Boolean,
        reads: List<ChannelUserRead>,
        deletedMessageVisibility: DeletedMessageVisibility,
        dateSeparatorHandler: DateSeparatorHandler,
        messageFooterVisibility: MessageFooterVisibility,
        messagePositionHandler: MessagePositionHandler,
        typingUsers: List<User>,
        focusedMessage: Message?,
        unreadLabel: UnreadLabel?,
        members: List<Member>,
        endOfOlderMessages: Boolean,
        channel: Channel?,
    ): List<MessageListItemState> {
        val parentMessageId = (_mode.value as? MessageMode.MessageThread)?.parentMessage?.id
        val currentUser = user.value
        val groupedMessages = mutableListOf<MessageListItemState>()
        val membersMap = members.associateBy { it.user.id }
        val sortedReads = reads
            .filter { it.user.id != currentUser?.id && !it.belongsToFreshlyAddedMember(membersMap) }
            .sortedBy { it.lastRead }
        val lastRead = sortedReads.lastOrNull()?.lastRead

        val isThreadWithNoReplies = isInThread && messages.size == 1
        val isThreadWithReplies = isInThread && messages.size > 1
        val shouldAddDateSeparatorInEmptyThread = isThreadWithNoReplies && showDateSeparatorInEmptyThread
        val shouldAddThreadSeparator = isThreadWithReplies ||
            (isThreadWithNoReplies && showThreadSeparatorInEmptyThread)

        if (endOfOlderMessages && channel != null) {
            groupedMessages.add(StartOfTheChannelItemState(channel))
        }

        messages.forEachIndexed { index, message ->
            val user = message.user
            val previousMessage = messages.getOrNull(index - 1)
            val nextMessage = messages.getOrNull(index + 1)

            val shouldAddDateSeparator = dateSeparatorHandler.shouldAddDateSeparator(previousMessage, message)

            val position = messagePositionHandler.handleMessagePosition(
                previousMessage = previousMessage,
                message = message,
                nextMessage = nextMessage,
                isAfterDateSeparator = shouldAddDateSeparator,
                isInThread = isInThread,
            )

            val isLastMessageInGroup =
                position.contains(MessagePosition.BOTTOM) || position.contains(MessagePosition.NONE)

            val shouldShowFooter = messageFooterVisibility.shouldShowMessageFooter(
                message = message,
                isLastMessageInGroup = isLastMessageInGroup,
                nextMessage = nextMessage,
            )

            if (shouldAddDateSeparator) {
                message.getCreatedAtOrNull()?.let { createdAt ->
                    groupedMessages.add(DateSeparatorItemState(createdAt))
                }
            }

            if (message.isSystem() || (message.isError() && !message.isModerationBounce())) {
                groupedMessages.add(SystemMessageItemState(message = message))
            } else {
                val isMessageRead = message.createdAt
                    ?.let { lastRead != null && it <= lastRead }
                    ?: false

                val messageReadBy = message.createdAt?.let { messageCreatedAt ->
                    sortedReads.filter { it.lastRead.after(messageCreatedAt) ?: false }
                } ?: emptyList()

                val isMessageFocused = message.id == focusedMessage?.id
                if (isMessageFocused) removeMessageFocus(message.id)

                groupedMessages.add(
                    MessageItemState(
                        message = message,
                        currentUser = currentUser,
                        groupPosition = position,
                        parentMessageId = parentMessageId,
                        isMine = user.id == currentUser?.id,
                        isInThread = isInThread,
                        isMessageRead = isMessageRead,
                        deletedMessageVisibility = deletedMessageVisibility,
                        showMessageFooter = shouldShowFooter,
                        messageReadBy = messageReadBy,
                        focusState = if (isMessageFocused) MessageFocused else null,
                    ),
                )
            }

            unreadLabel
                ?.takeIf { it.lastReadMessageId == message.id }
                ?.takeIf { nextMessage != null }
                ?.let { groupedMessages.add(UnreadSeparatorItemState(it.unreadCount)) }

            if (index == 0 && shouldAddThreadSeparator) {
                groupedMessages.add(
                    ThreadDateSeparatorItemState(
                        date = message.getCreatedAtOrDefault(Date()),
                        replyCount = message.replyCount,
                    ),
                )
            }

            if (shouldAddDateSeparatorInEmptyThread) {
                message.getCreatedAtOrNull()?.let { createdAt ->
                    groupedMessages.add(DateSeparatorItemState(createdAt))
                }
            }

            if (isThreadWithNoReplies) {
                groupedMessages.add(EmptyThreadPlaceholderItemState)
            }
        }

        if (typingUsers.isNotEmpty()) {
            groupedMessages.add(TypingItemState(typingUsers))
        }

        return groupedMessages
    }


    private fun ChannelUserRead.belongsToFreshlyAddedMember(
        membersMap: Map<String, Member>,
    ): Boolean {
        val member = membersMap[user.id]
        val membershipAndLastReadDiff = member?.createdAt?.diff(lastRead)?.millis ?: Long.MAX_VALUE
        return membershipAndLastReadDiff < MEMBERSHIP_AND_LAST_READ_THRESHOLD_MS
    }

    private fun filterMessagesToShow(
        messages: List<Message>,
        showSystemMessages: Boolean,
        deletedMessageVisibility: DeletedMessageVisibility,
    ): List<Message> {
        val currentUser = user.value

        return messages.filter {
            val shouldNotShowIfDeleted = when (deletedMessageVisibility) {
                DeletedMessageVisibility.ALWAYS_VISIBLE -> true
                DeletedMessageVisibility.VISIBLE_FOR_CURRENT_USER -> {
                    !(it.isDeleted() && it.user.id != currentUser?.id)
                }
                DeletedMessageVisibility.ALWAYS_HIDDEN -> !it.isDeleted()
            }
            val isSystemMessage = it.isSystem() || it.isError()

            shouldNotShowIfDeleted || (isSystemMessage && showSystemMessages)
        }
    }

    private fun getNewMessageState(lastMessage: Message?, lastLoadedMessage: Message?): NewMessageState? {
        val lastLoadedMessageDate = lastLoadedMessage?.createdAt ?: lastLoadedMessage?.createdLocallyAt

        return when {
            lastMessage == null -> null
            lastLoadedMessage == null -> getNewMessageStateForMessage(lastMessage)
            lastMessage.wasCreatedAfter(lastLoadedMessageDate) &&
                (lastMessage.isGiphy() || lastLoadedMessage.id != lastMessage.id) -> {
                getNewMessageStateForMessage(lastMessage)
            }
            else -> getNewMessageStateForMessage(lastMessage)
        }
    }


    private fun getNewMessageStateForMessage(message: Message): NewMessageState {
        val currentUser = user.value
        return when (message.user.id == currentUser?.id) {
            true -> MyOwn(ts = message.getCreatedAtOrNull()?.time)
            else -> Other(ts = message.createdAt?.time)
        }
    }

    public fun scrollToBottom(messageLimit: Int = this.messageLimit, scrollToBottom: () -> Unit) {
        if (isInThread || channelState.value?.endOfNewerMessages?.value == true) {
            scrollToBottom()
        } else {
            chatClient.loadNewestMessages(cid, messageLimit).enqueue { result ->
                when (result) {
                    is Result.Success -> scrollToBottom()
                    is Result.Failure ->
                        logger.e {
                            "Could not load newest messages. Message: ${result.value.message}. " +
                                "Cause: ${result.value.extractCause()}"
                        }
                }
            }
        }
    }

    public fun loadNewerMessages(baseMessageId: String, messageLimit: Int = this.messageLimit) {
        logger.i { "[loadNewerMessages] baseMessageId: $baseMessageId, messageLimit: $messageLimit" }
        if (clientState.isOffline) return
        _mode.value.run {
            when (this) {
                is MessageMode.Normal -> loadNewerChannelMessages(baseMessageId, messageLimit)
                is MessageMode.MessageThread -> loadNewerMessagesInThread(this)
            }
        }
    }

    private fun loadNewerChannelMessages(baseMessageId: String, messageLimit: Int = this.messageLimit) {
        if (channelState.value?.endOfNewerMessages?.value == true) {
            logger.d {
                "[loadNewerChannelMessages] rejected; endOfNewerMessages: " +
                    "${channelState.value?.endOfNewerMessages?.value}"
            }
            return
        }
        chatClient.loadNewerMessages(cid, baseMessageId, messageLimit).enqueue()
    }

    private fun loadNewerMessagesInThread(
        threadMode: MessageMode.MessageThread,
    ) {
        logger.d {
            "[loadNewerMessagesInThread] endOfNewerMessages: ${threadMode.threadState?.endOfNewerMessages?.value}"
        }
        if (threadMode.threadState?.endOfNewerMessages?.value == true ||
            threadMode.threadState?.loading?.value == true ||
            !threadLoadOrderOlderToNewer
        ) {
            logger.d {
                "[loadNewerMessagesInThread] rejected; " +
                    "endOfNewerMessages: ${threadMode.threadState?.endOfNewerMessages?.value}, " +
                    "loading: ${threadMode.threadState?.loading?.value}, " +
                    "threadLoadOrderOlderToNewer: $threadLoadOrderOlderToNewer"
            }
            return
        }
        logger.d {
            "[loadNewerMessagesInThread] loading newer messages:" +
                "parentId: ${threadMode.parentMessage.id}, " +
                "messageLimit: $messageLimit, " +
                "lastId = ${threadMode.threadState?.newestInThread?.value?.id}"
        }
        chatClient.getNewerReplies(
            parentId = threadMode.parentMessage.id,
            limit = messageLimit,
            lastId = threadMode.threadState?.newestInThread?.value?.id,
        ).enqueue()
    }

    public fun loadOlderMessages(messageLimit: Int = this.messageLimit) {
        logger.i { "[loadOlderMessages] messageLimit: $messageLimit" }
        if (clientState.isOffline) return

        _mode.value.run {
            when (this) {
                is MessageMode.Normal -> {
                    if (channelState.value?.endOfOlderMessages?.value == true) return
                    chatClient.loadOlderMessages(cid, messageLimit).enqueue()
                }
                is MessageMode.MessageThread -> threadLoadMore(this)
            }
        }
    }

    @Suppress("ComplexCondition")
    private fun threadLoadMore(threadMode: MessageMode.MessageThread, messageLimit: Int = this.messageLimit) {
        if (_threadListState.value.endOfOldMessagesReached ||
            _threadListState.value.isLoadingOlderMessages ||
            threadLoadOrderOlderToNewer ||
            threadMode.threadState?.oldestInThread?.value == null
        ) {
            return
        }

        _threadListState.value = _threadListState.value.copy(isLoadingOlderMessages = true)
        chatClient.getRepliesMore(
            messageId = threadMode.parentMessage.id,
            firstId = threadMode.threadState.oldestInThread.value?.id ?: threadMode.parentMessage.id,
            limit = messageLimit,
        ).enqueue {
            _threadListState.value = _threadListState.value.copy(isLoadingOlderMessages = false)
        }
    }

    public suspend fun enterThreadMode(parentMessage: Message, messageLimit: Int = this.messageLimit) {
        val channelState = channelState.value ?: return
        _messageActions.value = _messageActions.value + Reply(parentMessage)

        val state = chatClient.getRepliesAsState(parentMessage.id, messageLimit, threadLoadOrderOlderToNewer)

        _mode.value = MessageMode.MessageThread(parentMessage, state)
        observeThreadMessagesState(
            threadId = state.parentId,
            messages = state.messages,
            endOfOlderMessages = state.endOfOlderMessages,
            reads = channelState.reads,
            members = channelState.members,
        )
    }

    private suspend fun enterThreadSequential(parentMessage: Message) {
        logger.v { "[enterThreadSequential] parentMessage(id: ${parentMessage.id}, text: ${parentMessage.text})" }
        val threadState = chatClient.awaitRepliesAsState(
            parentMessage.id,
            DEFAULT_MESSAGES_LIMIT,
            threadLoadOrderOlderToNewer,
        )
        val channelState = channelState.value ?: return

        _messageActions.value = _messageActions.value + Reply(parentMessage)
        _mode.value = MessageMode.MessageThread(parentMessage, threadState)

        observeThreadMessagesState(
            threadId = threadState.parentId,
            messages = threadState.messages,
            endOfOlderMessages = threadState.endOfOlderMessages,
            reads = channelState.reads,
            members = channelState.members,
        )
    }

    private suspend fun enterThreadSequential(parentMessageId: String) {
        logger.v { "[enterThreadSequential] parentMessageId: $parentMessageId" }
        val result = chatClient.getMessageUsingCache(parentMessageId).await()

        when (result) {
            is Result.Success -> {
                enterThreadSequential(result.value)
            }
            is Result.Failure ->
                logger.e {
                    "[enterThreadSequential] -> Could not get message: ${result.value.message}."
                }
        }
    }

    public fun enterNormalMode() {
        _mode.value = MessageMode.Normal
        _threadListState.value = MessageListState()
        lastLoadedThreadMessage = null
        threadJob?.cancel()
    }

    public fun loadMessageById(messageId: String, onResult: (Result<Message>) -> Unit = {}) {
        logger.i { "[loadMessageById] messageId: $messageId" }
        chatClient.loadMessageById(cid, messageId).enqueue { result ->
            onResult(result)
            if (result is Result.Failure) {
                val error = result.value
                logger.e {
                    "Could not load the message with id: $messageId inside channel: $cid. " +
                        "Error: ${error.extractCause()}. Message: ${error.message}"
                }
            }
        }
    }

    public fun scrollToMessage(
        messageId: String,
        parentMessageId: String?,
    ) {
        focusMessage(messageId, parentMessageId)
    }

    private fun focusMessage(
        messageId: String,
        parentMessageId: String?,
    ) {
        logger.v { "[focusMessage] messageId: $messageId, parentMessageId: $parentMessageId" }
        if (parentMessageId == null) {
            focusChannelMessage(messageId)
        } else {
            focusThreadMessage(
                threadMessageId = messageId,
                parentMessageId = parentMessageId,
            )
        }
    }

    private fun focusChannelMessage(messageId: String) {
        logger.v { "[focusChannelMessage] messageId: $messageId" }
        val message = getMessageFromListStateById(messageId)

        if (message != null) {
            focusedMessage.value = message
        } else {
            loadMessageById(messageId) { result ->
                focusedMessage.value = when (result) {
                    is Result.Success -> result.value
                    is Result.Failure -> {
                        logger.e {
                            "[focusChannelMessage] -> Could not load message: ${result.value.message}."
                        }

                        null
                    }
                }
            }
        }
    }

    private fun focusThreadMessage(
        threadMessageId: String,
        parentMessageId: String,
    ) {
        scope.launch {
            val mode = _mode.value
            if (mode !is MessageMode.MessageThread || mode.parentMessage.id != parentMessageId) {
                enterThreadSequential(parentMessageId)
            }

            val threadMessageResult = chatClient.getMessageUsingCache(messageId = threadMessageId).await()

            focusedMessage.value = when (threadMessageResult) {
                is Result.Success -> threadMessageResult.value
                is Result.Failure -> {
                    logger.e {
                        "[focusThreadMessage] -> Could not focus thread parent: ${threadMessageResult.value.message}."
                    }

                    null
                }
            }
        }
    }

    private fun removeMessageFocus(messageId: String) {
        if (removeFocusedMessageJob?.first != messageId) {
            removeFocusedMessageJob = messageId to scope.launch {
                delay(REMOVE_MESSAGE_FOCUS_DELAY)

                val messages = messagesState.messageItems.map {
                    if (it is MessageItemState && it.message.id == messageId) {
                        it.copy(focusState = MessageFocusRemoved)
                    } else {
                        it
                    }
                }
                setMessageListState(_messageListState.value.copy(messageItems = messages))

                if (focusedMessage.value?.id == messageId) {
                    focusedMessage.value = null
                    removeFocusedMessageJob = null
                }
            }
        }
    }

    public fun selectMessage(message: Message?) {
        changeSelectMessageState(
            message?.let {
                val currentUserId = chatClient.getCurrentUser()?.id
                if (it.isModerationError(currentUserId)) {
                    SelectedMessageFailedModerationState(
                        message = it,
                        ownCapabilities = ownCapabilities.value,
                    )
                } else {
                    SelectedMessageOptionsState(
                        message = it,
                        ownCapabilities = ownCapabilities.value,
                    )
                }
            },
        )
    }

    public fun selectReactions(message: Message?) {
        if (message != null) {
            changeSelectMessageState(
                SelectedMessageReactionsState(
                    message = message,
                    ownCapabilities = ownCapabilities.value,
                ),
            )
        }
    }

    public fun selectExtendedReactions(message: Message?) {
        if (message != null) {
            changeSelectMessageState(
                SelectedMessageReactionsPickerState(
                    message = message,
                    ownCapabilities = ownCapabilities.value,
                ),
            )
        }
    }

    private fun changeSelectMessageState(selectedMessageState: SelectedMessageState?) {
        if (isInThread) {
            _threadListState.value = _threadListState.value.copy(selectedMessageState = selectedMessageState)
        } else {
            setMessageListState(_messageListState.value.copy(selectedMessageState = selectedMessageState))
        }
    }

    public suspend fun performMessageAction(messageAction: MessageAction) {
        removeOverlay()

        when (messageAction) {
            is Resend -> resendMessage(messageAction.message)
            is ThreadReply -> {
                enterThreadMode(messageAction.message)
            }
            is Delete, is FlagMessage -> {
                _messageActions.value = _messageActions.value + messageAction
            }
            is Copy -> copyMessage(messageAction.message)
            is React -> reactToMessage(messageAction.reaction, messageAction.message)
            is Pin -> updateMessagePin(messageAction.message)
            is MarkAsUnread -> markUnread(messageAction.message)
            else -> {

            }
        }
    }

    public fun dismissMessageAction(messageAction: MessageAction) {
        _messageActions.value = _messageActions.value - messageAction
    }

    public fun dismissAllMessageActions() {
        _messageActions.value = emptySet()
    }

    private fun copyMessage(message: Message) {
        clipboardHandler.copyMessage(message)
    }

    public fun removeOverlay() {
        _threadListState.value = _threadListState.value.copy(selectedMessageState = null)
        setMessageListState(_messageListState.value.copy(selectedMessageState = null))
    }

    public fun deleteMessage(message: Message, hard: Boolean = false) {
        _messageActions.value = _messageActions.value - _messageActions.value.filterIsInstance<Delete>().toSet()
        removeOverlay()

        chatClient.deleteMessage(message.id, hard)
            .enqueue(
                onError = { error ->
                    logger.e {
                        "Could not delete message: ${error.message}, Hard: $hard. " +
                            "Cause: ${error.extractCause()}. If you're using OfflinePlugin, the message " +
                            "should be deleted in the database and it will be deleted in the backend when " +
                            "the SDK sync its information."
                    }
                },
            )
    }

    public fun updateLastSeenMessage(message: Message) {
        val lastLoadedMessage = if (isInThread) lastLoadedThreadMessage else lastLoadedMessage
        logger.d {
            "[updateLastSeenMessage] isInThread: $isInThread, message: ${message.id}('${message.text}'), " +
                "lastLoadedMessage: ${lastLoadedMessage?.id}('${lastLoadedMessage?.text}')"
        }
        if (message.id == lastLoadedMessage?.id) {
            logger.v { "[updateLastSeenMessage] matched(isInThread: $isInThread)" }
            markLastMessageRead()
        }
    }

    public fun markLastMessageRead() {
        logger.v { "[markLastMessageRead] cid: $cid" }
        debouncer.submit {
            markLastMessageReadInternal()
        }
    }

    private fun markLastMessageReadInternal() {
        val itemState = messagesState.messageItems.lastOrNull { messageItem ->
            messageItem is HasMessageListItemState
        } as? HasMessageListItemState
        val messageId = itemState?.message?.id
        val messageText = itemState?.message?.text
        logger.d { "[markLastMessageRead] cid: $cid, msgId($isInThread): $messageId, msgText: \"$messageText\"" }

        val lastSeenMessageId = this.lastSeenMessageId
        if (lastSeenMessageId == messageId) {
            logger.w { "[markLastMessageRead] cid: $cid; rejected[$isInThread] (already seen msgId): $messageId" }
            return
        }
        this.lastSeenMessageId = messageId

        cid.cidToTypeAndId().let { (channelType, channelId) ->
            if (isInThread) {
            } else {
                chatClient.markRead(channelType, channelId).enqueue(
                    onError = { error ->
                        logger.e {
                            "Could not mark cid: $channelId as read. Error message: ${error.message}. " +
                                "Cause: ${error.extractCause()}"
                        }
                    },
                )
            }
        }
    }


    public fun flagMessage(
        message: Message,
        reason: String?,
        customData: Map<String, String>,
        onResult: (Result<Flag>) -> Unit = {},
    ) {
        _messageActions.value = _messageActions.value - _messageActions.value.filterIsInstance<FlagMessage>().toSet()
        chatClient.flagMessage(
            message.id,
            reason,
            customData,
        ).enqueue { response ->
            onResult(response)
            if (response is Result.Failure) {
                val error = response.value
                onActionResult(error) {
                    ErrorEvent.FlagMessageError(it)
                }
            }
        }
    }

    public fun markUnread(message: Message, onResult: (Result<Unit>) -> Unit = {}) {
        cid.cidToTypeAndId().let { (channelType, channelId) ->
            chatClient.markUnread(channelType, channelId, message.id).enqueue { response ->
                onResult(response)
                if (response is Result.Failure) {
                    onActionResult(response.value) {
                        ErrorEvent.MarkUnreadError(it)
                    }
                } else {
                    refreshUnreadLabel(false)
                }
            }
        }
    }


    public fun updateMessagePin(message: Message) {
        if (message.pinned) {
            unpinMessage(message)
        } else {
            pinMessage(message)
        }
    }

    public fun pinMessage(message: Message) {
        chatClient.pinMessage(message).enqueue(onError = { error ->
            onActionResult(error) {
                ErrorEvent.PinMessageError(it)
            }
        })
    }

    public fun unpinMessage(message: Message) {
        chatClient.unpinMessage(message).enqueue(onError = { error ->
            onActionResult(error) {
                ErrorEvent.UnpinMessageError(it)
            }
        })
    }

    public fun resendMessage(message: Message) {
        val (channelType, channelId) = message.cid.cidToTypeAndId()
        chatClient.sendMessage(channelType, channelId, message)
            .enqueue(onError = { error ->
                logger.e {
                    "(Retry) Could not send message: ${error.message}. " +
                        "Cause: ${error.extractCause()}"
                }
            })
    }


    public fun updateUserMute(user: User) {
        val isUserMuted = chatClient.globalState.muted.value.any { it.target.id == user.id }

        if (isUserMuted) {
            unmuteUser(user)
        } else {
            muteUser(user)
        }
    }

    public fun muteUser(user: User) {
        chatClient.muteUser(user.id).enqueue(onError = { error ->
            onActionResult(error) {
                ErrorEvent.MuteUserError(it)
            }
        })
    }

    public fun unmuteUser(user: User) {
        chatClient.unmuteUser(user.id).enqueue(onError = { error ->
            onActionResult(error) {
                ErrorEvent.UnmuteUserError(it)
            }
        })
    }

    public fun reactToMessage(reaction: Reaction, message: Message) {
        if (message.ownReactions.any { it.type == reaction.type }) {
            chatClient.deleteReaction(
                messageId = message.id,
                reactionType = reaction.type,
                cid = cid,
            ).enqueue(
                onError = { error ->
                    logger.e {
                        "Could not delete reaction for message with id: ${reaction.messageId} " +
                            "Error: ${error.message}. Cause: ${error.extractCause()}"
                    }
                },
            )
        } else {
            chatClient.sendReaction(
                enforceUnique = enforceUniqueReactions,
                reaction = reaction,
                cid = cid,
            ).enqueue(
                onError = { streamError ->
                    logger.e {
                        "Could not send reaction for message with id: ${reaction.messageId} " +
                            "Error: ${streamError.message}. Cause: ${streamError.extractCause()}"
                    }
                },
            )
        }
    }

    private fun showEmptyState() {
        logger.d { "[showEmptyState] no args" }
        setMessageListState(_messageListState.value.copy(isLoading = false, messageItems = emptyList()))
    }

    public fun getMessageFromListStateById(messageId: String): Message? {
        return (
            listState.value.messageItems.firstOrNull {
                it is MessageItemState && it.message.id == messageId
            } as? MessageItemState
            )?.message
    }

    public fun clearNewMessageState() {
        logger.d { "[clearNewMessageState] no args" }
        if (!messagesState.endOfNewMessagesReached) return
        _threadListState.value = _threadListState.value.copy(newMessageState = null, unreadCount = 0)
        setMessageListState(_messageListState.value.copy(newMessageState = null, unreadCount = 0))
    }


    public fun muteUser(userId: String, timeout: Int? = null) {
        chatClient.muteUser(userId, timeout)
            .enqueue(onError = { streamError ->
                val errorMessage = streamError.message
                logger.e { errorMessage }
            })
    }

    public fun unmuteUser(userId: String) {
        chatClient.unmuteUser(userId)
            .enqueue(onError = { streamError ->
                val errorMessage = streamError.message
                logger.e { errorMessage }
            })
    }

    public fun banUser(
        userId: String,
        reason: String? = null,
        timeout: Int? = null,
    ) {
        chatClient.channel(cid).banUser(userId, reason, timeout).enqueue(onError = { error ->
            onActionResult(error) {
                ErrorEvent.BlockUserError(it)
            }
        })
    }

    public fun unbanUser(userId: String) {
        chatClient.channel(cid).unbanUser(userId).enqueue(onError = { error ->
            onActionResult(error) {
                ErrorEvent.BlockUserError(it)
            }
        })
    }


    public fun shadowBanUser(
        userId: String,
        reason: String? = null,
        timeout: Int? = null,
    ) {
        chatClient.channel(cid).shadowBanUser(userId, reason, timeout).enqueue(onError = { error ->
            onActionResult(error) {
                ErrorEvent.BlockUserError(it)
            }
        })
    }

    public fun removeShadowBanFromUser(userId: String) {
        chatClient.channel(cid).removeShadowBan(userId).enqueue(onError = { error ->
            onActionResult(error) {
                ErrorEvent.BlockUserError(it)
            }
        })
    }


    public fun performGiphyAction(action: GiphyAction) {
        val message = action.message
        when (action) {
            is SendGiphy -> chatClient.sendGiphy(message)
            is ShuffleGiphy -> chatClient.shuffleGiphy(message)
            is CancelGiphy -> chatClient.cancelEphemeralMessage(message)
        }.exhaustive.enqueue(onError = { streamError ->
            logger.e {
                "Could not ${action::class.java.simpleName} giphy for message id: ${message.id}. " +
                    "Error: ${streamError.message}. Cause: ${streamError.extractCause()}"
            }
        })
    }


    public fun removeAttachment(messageId: String, attachmentToBeDeleted: Attachment) {
        logger.d { "[removeAttachment] messageId: $messageId, attachmentToBeDeleted: $attachmentToBeDeleted" }
        chatClient.loadMessageById(
            cid,
            messageId,
        ).enqueue { result ->
            when (result) {
                is Result.Success -> {
                    val message = result.value.copy(
                        attachments = result.value.attachments.filterNot { attachment ->
                            val imageUrl = attachmentToBeDeleted.imageUrl
                            val assetUrl = attachmentToBeDeleted.assetUrl
                            when {
                                assetUrl != null -> {
                                    attachment.assetUrl?.substringBefore("?") ==
                                        assetUrl.substringBefore("?")
                                }
                                imageUrl != null -> {
                                    attachment.imageUrl?.substringBefore("?") ==
                                        imageUrl.substringBefore("?")
                                }
                                else -> false
                            }
                        },
                    )

                    if (message.text.isBlank() && message.attachments.isEmpty()) {
                        chatClient.deleteMessage(messageId = messageId).enqueue(
                            onError = { streamError ->
                                logger.e {
                                    "Could not remove the attachment and delete the remaining blank message" +
                                        ": ${streamError.message}. Cause: ${streamError.extractCause()}"
                                }
                            },
                        )
                    } else {
                        chatClient.updateMessage(message).enqueue(
                            onError = { streamError ->
                                logger.e {
                                    "Could not edit message to remove its attachments: ${streamError.message}. " +
                                        "Cause: ${streamError.extractCause()}"
                                }
                            },
                        )
                    }
                }
                is Result.Failure -> logger.e { "Could not load message: ${result.value}" }
            }
        }
    }

    public fun setMessagePositionHandler(messagePositionHandler: MessagePositionHandler) {
        _messagePositionHandler.value = messagePositionHandler
    }

    public fun setDateSeparatorHandler(dateSeparatorHandler: DateSeparatorHandler?) {
        _dateSeparatorHandler.value = dateSeparatorHandler ?: DateSeparatorHandler { _, _ -> false }
    }

    public fun setThreadDateSeparatorHandler(threadDateSeparatorHandler: DateSeparatorHandler?) {
        _threadDateSeparatorHandler.value = threadDateSeparatorHandler ?: DateSeparatorHandler { _, _ -> false }
    }

    public fun setMessageFooterVisibility(messageFooterVisibility: MessageFooterVisibility) {
        _messageFooterVisibilityState.value = messageFooterVisibility
    }

    public fun setDeletedMessageVisibility(deletedMessageVisibility: DeletedMessageVisibility) {
        _deletedMessageVisibilityState.value = deletedMessageVisibility
    }

    public fun setSystemMessageVisibility(areSystemMessagesVisible: Boolean) {
        _showSystemMessagesState.value = areSystemMessagesVisible
    }

    private fun onActionResult(
        error: Error,
        onError: (Error) -> ErrorEvent,
    ) {
        val errorMessage = error.message
        logger.e { errorMessage }
        _errorEvents.value = onError(error)
    }


    public fun onCleared() {
        scope.cancel()
    }


    public sealed class ErrorEvent(public open val streamError: Error) {

        public data class MuteUserError(override val streamError: Error) : ErrorEvent(streamError)

        public data class UnmuteUserError(override val streamError: Error) : ErrorEvent(streamError)

        public data class FlagMessageError(override val streamError: Error) : ErrorEvent(streamError)

        public data class MarkUnreadError(override val streamError: Error) : ErrorEvent(streamError)

        public data class BlockUserError(override val streamError: Error) : ErrorEvent(streamError)

        public data class PinMessageError(override val streamError: Error) : ErrorEvent(streamError)


        public data class UnpinMessageError(override val streamError: Error) : ErrorEvent(streamError)
    }

    public data class UnreadLabel(
        val unreadCount: Int,
        val lastReadMessageId: String,
        val buttonVisibility: Boolean,
    )

    public companion object {
        public const val DEFAULT_MESSAGES_LIMIT: Int = 30

        internal const val REMOVE_MESSAGE_FOCUS_DELAY: Long = 2000

        internal const val MEMBERSHIP_AND_LAST_READ_THRESHOLD_MS = 100L
    }
}

private fun MessageListItemState.stringify(): String {
    return when (this) {
        is DateSeparatorItemState -> "DateSeparator"
        is EmptyThreadPlaceholderItemState -> "EmptyThreadPlaceholder"
        is MessageItemState -> message.text
        is SystemMessageItemState -> message.text
        is ThreadDateSeparatorItemState -> "ThreadDateSeparator"
        is TypingItemState -> "Typing"
        is UnreadSeparatorItemState -> "UnreadSeparator"
        is StartOfTheChannelItemState -> "StartOfTheChannelItemState"
    }
}
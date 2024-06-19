package com.example.composechatsample.viewModel

import com.example.composechatsample.common.RecordingState
import com.example.composechatsample.common.StreamMediaRecorder
import com.example.composechatsample.common.UserLookupHandler
import com.example.composechatsample.core.Call
import com.example.composechatsample.core.ChannelState
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.DispatcherProvider
import com.example.composechatsample.core.cidToTypeAndId
import com.example.composechatsample.core.isModerationError
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.ChannelCapabilities
import com.example.composechatsample.core.models.Command
import com.example.composechatsample.core.models.LinkPreview
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.log.StreamLog
import com.example.composechatsample.log.TaggedLogger
import com.example.composechatsample.screen.messages.Edit
import com.example.composechatsample.screen.messages.MessageAction
import com.example.composechatsample.screen.messages.MessageComposerState
import com.example.composechatsample.screen.messages.MessageMode
import com.example.composechatsample.screen.messages.Reply
import com.example.composechatsample.screen.messages.ThreadReply
import com.example.composechatsample.screen.messages.ValidationError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import java.util.concurrent.TimeUnit
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.addSchemeToUrlIfNeeded
import com.example.composechatsample.core.map
import java.util.regex.Pattern

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
public class MessageComposerController(
    private val channelCid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val mediaRecorder: StreamMediaRecorder,
    private val userLookupHandler: UserLookupHandler,
    private val fileToUri: (File) -> String,
    private val messageLimit: Int,
    maxAttachmentCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT,
    private val messageId: String? = null,
    private val isLinkPreviewEnabled: Boolean = false,
) {

    private val messageValidator = MessageValidator(
        appSettings = chatClient.getAppSettings(),
        maxAttachmentCount = maxAttachmentCount,
    )

    private val logger: TaggedLogger = StreamLog.getLogger("Chat:MessageComposerController")

    private val scope = CoroutineScope(DispatcherProvider.Immediate)

    private val audioRecordingController = AudioRecordingController(
        channelCid,
        chatClient.audioPlayer,
        mediaRecorder,
        fileToUri,
        scope,
    )

    public var typingUpdatesBuffer: TypingUpdatesBuffer = DefaultTypingUpdatesBuffer(
        onTypingStarted = ::sendKeystrokeEvent,
        onTypingStopped = ::sendStopTypingEvent,
        coroutineScope = scope,
    )

    private val _channelState: StateFlow<ChannelState?> = observeChannelState()

    public val channelState: Flow<ChannelState> = _channelState.filterNotNull()

    public val ownCapabilities: StateFlow<Set<String>> = channelState.flatMapLatest { it.channelData }
        .map {
            messageValidator.canSendLinks = it.ownCapabilities.contains(ChannelCapabilities.SEND_LINKS)
            it.ownCapabilities
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = setOf(),
        )

    private val canSendTypingUpdates = ownCapabilities
        .map { it.contains(ChannelCapabilities.TYPING_EVENTS) || it.contains(ChannelCapabilities.SEND_TYPING_EVENTS) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    private val isSlowModeActive = ownCapabilities.map { it.contains(ChannelCapabilities.SLOW_MODE) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    private val isSlowModeDisabled = ownCapabilities.map { it.contains(ChannelCapabilities.SKIP_SLOW_MODE) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    public val state: MutableStateFlow<MessageComposerState> = MutableStateFlow(MessageComposerState())

    public val messageInput: MutableStateFlow<MessageInput> = MutableStateFlow(MessageInput())

    @Deprecated(
        message = "Use messageInput instead",
        replaceWith = ReplaceWith("messageInput"),
    )
    public val input: MutableStateFlow<String> = MutableStateFlow("")

    public val alsoSendToChannel: MutableStateFlow<Boolean> = MutableStateFlow(false)

    public val cooldownTimer: MutableStateFlow<Int> = MutableStateFlow(0)

    public val selectedAttachments: MutableStateFlow<List<Attachment>> = MutableStateFlow(emptyList())

    public val validationErrors: MutableStateFlow<List<ValidationError>> = MutableStateFlow(emptyList())

    public val mentionSuggestions: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())

    public val commandSuggestions: MutableStateFlow<List<Command>> = MutableStateFlow(emptyList())

    public val linkPreviews: MutableStateFlow<List<LinkPreview>> = MutableStateFlow(emptyList())

    private var users: List<User> = emptyList()

    private var commands: List<Command> = emptyList()

    private var cooldownTimerJob: Job? = null

    public val messageMode: MutableStateFlow<MessageMode> = MutableStateFlow(MessageMode.Normal)

    public val messageActions: MutableStateFlow<Set<MessageAction>> = MutableStateFlow(mutableSetOf())

    public val lastActiveAction: Flow<MessageAction?>
        get() = messageActions.map { actions ->
            actions.lastOrNull { it is Edit || it is Reply }
        }

    private val activeAction: MessageAction?
        get() = messageActions.value.lastOrNull { it is Edit || it is Reply }

    private val isInEditMode: Boolean
        get() = activeAction is Edit

    private val parentMessageId: String?
        get() = (messageMode.value as? MessageMode.MessageThread)?.parentMessage?.id

    private val messageText: String
        get() = messageInput.value.text

    private val isInThread: Boolean
        get() = messageMode.value is MessageMode.MessageThread

    private val selectedMentions: MutableSet<User> = mutableSetOf()

    private val mentionSuggester = TypingSuggester(
        TypingSuggestionOptions(symbol = MentionStartSymbol),
    )


    init {
        channelState.flatMapLatest { it.channelConfig }.onEach {
            messageValidator.maxMessageLength = it.maxMessageLength
            commands = it.commands
            state.value = state.value.copy(hasCommands = commands.isNotEmpty())
        }.launchIn(scope)

        channelState.flatMapLatest { it.members }.onEach { members ->
            users = members.map { it.user }
        }.launchIn(scope)

        channelState.flatMapLatest { combine(it.channelData, it.lastSentMessageDate, ::Pair) }
            .distinctUntilChangedBy { (_, lastSentMessageDate) -> lastSentMessageDate }
            .onEach { (channelData, lastSentMessageDate) ->
                handleLastSentMessageDate(channelData.cooldown, lastSentMessageDate)
            }.launchIn(scope)

        setupComposerState()
    }

    private fun observeChannelState(): StateFlow<ChannelState?> {
        logger.d { "[observeChannelState] cid: $channelCid, messageId: $messageId, messageLimit: $messageLimit" }
        return chatClient.watchChannelAsState(
            cid = channelCid,
            messageLimit = messageLimit,
            coroutineScope = scope,
        )
    }


    @OptIn(FlowPreview::class)
    private fun setupComposerState() {
        messageInput.onEach { value ->
            input.value = value.text
            state.value = state.value.copy(inputValue = value.text)

            if (canSendTypingUpdates.value) {
                typingUpdatesBuffer.onKeystroke(value.text)
            }
            handleCommandSuggestions()
            handleValidationErrors()
        }.debounce(TEXT_INPUT_DEBOUNCE_TIME).onEach {
            scope.launch { handleMentionSuggestions() }
            scope.launch { handleLinkPreviews() }
        }.launchIn(scope)

        selectedAttachments.onEach { selectedAttachments ->
            state.value = state.value.copy(attachments = selectedAttachments)
        }.launchIn(scope)

        lastActiveAction.onEach { activeAction ->
            state.value = state.value.copy(action = activeAction)
        }.launchIn(scope)

        validationErrors.onEach { validationErrors ->
            state.value = state.value.copy(validationErrors = validationErrors)
        }.launchIn(scope)

        mentionSuggestions.onEach { mentionSuggestions ->
            state.value = state.value.copy(mentionSuggestions = mentionSuggestions)
        }.launchIn(scope)

        commandSuggestions.onEach { commandSuggestions ->
            state.value = state.value.copy(commandSuggestions = commandSuggestions)
        }.launchIn(scope)

        linkPreviews.onEach { linkPreviews ->
            state.value = state.value.copy(linkPreviews = linkPreviews)
        }.launchIn(scope)

        cooldownTimer.onEach { cooldownTimer ->
            state.value = state.value.copy(coolDownTime = cooldownTimer)
        }.launchIn(scope)

        messageMode.onEach { messageMode ->
            state.value = state.value.copy(messageMode = messageMode)
        }.launchIn(scope)

        alsoSendToChannel.onEach { alsoSendToChannel ->
            state.value = state.value.copy(alsoSendToChannel = alsoSendToChannel)
        }.launchIn(scope)

        ownCapabilities.onEach { ownCapabilities ->
            state.value = state.value.copy(ownCapabilities = ownCapabilities)
        }.launchIn(scope)

        chatClient.clientState.user.onEach { currentUser ->
            state.value = state.value.copy(currentUser = currentUser)
        }.launchIn(scope)

        audioRecordingController.recordingState.onEach { recording ->
            logger.d { "[onRecordingState] recording: $recording" }
            state.value = state.value.copy(recording = recording)
            if (recording is RecordingState.Complete) {
                selectedAttachments.value = selectedAttachments.value + recording.attachment
            }
        }.launchIn(scope)
    }

    public fun setMessageInput(value: String) {
        if (this.messageInput.value.text == value) return
        this.messageInput.value = MessageInput(value, MessageInput.Source.External)
    }

    private fun setMessageInputInternal(value: String, source: MessageInput.Source) {
        if (this.messageInput.value.text == value) return
        this.messageInput.value = MessageInput(value, source)
    }


    public fun setMessageMode(messageMode: MessageMode) {
        this.messageMode.value = messageMode
    }

    public fun setAlsoSendToChannel(alsoSendToChannel: Boolean) {
        this.alsoSendToChannel.value = alsoSendToChannel
    }

    public fun performMessageAction(messageAction: MessageAction) {
        when (messageAction) {
            is ThreadReply -> {
                setMessageMode(MessageMode.MessageThread(messageAction.message))
            }
            is Reply -> {
                messageActions.value = messageActions.value + messageAction
            }
            is Edit -> {
                setMessageInputInternal(messageAction.message.text, MessageInput.Source.Edit)
                selectedAttachments.value = messageAction.message.attachments
                messageActions.value = messageActions.value + messageAction
            }
            else -> {
                // no op, custom user action
            }
        }
    }

    public fun dismissMessageActions() {
        if (isInEditMode) {
            setMessageInputInternal("", MessageInput.Source.Default)
            this.selectedAttachments.value = emptyList()
        }

        this.messageActions.value = emptySet()
    }

    public fun addSelectedAttachments(attachments: List<Attachment>) {
        logger.d { "[addSelectedAttachments] attachments: $attachments" }
        val newAttachments = (selectedAttachments.value + attachments).distinctBy {
            if (it.name != null && it.mimeType?.isNotEmpty() == true) {
                it.name
            } else {
                it
            }
        }
        selectedAttachments.value = newAttachments

        handleValidationErrors()
    }

    public fun removeSelectedAttachment(attachment: Attachment) {
        selectedAttachments.value = selectedAttachments.value - attachment

        handleValidationErrors()
    }

    public fun clearData() {
        logger.i { "[clearData]" }
        messageInput.value = MessageInput()
        selectedAttachments.value = emptyList()
        validationErrors.value = emptyList()
        alsoSendToChannel.value = false
    }

    public fun sendMessage(message: Message, callback: Call.Callback<Message>) {
        logger.i { "[sendMessage] message.attachments.size: ${message.attachments.size}" }
        val activeMessage = activeAction?.message ?: message

        val currentUserId = chatClient.getCurrentUser()?.id
        val sendMessageCall = if (isInEditMode && !activeMessage.isModerationError(currentUserId)) {
            getEditMessageCall(message)
        } else {
            val (channelType, channelId) = message.cid.cidToTypeAndId()
            if (activeMessage.isModerationError(currentUserId)) {
                chatClient.deleteMessage(activeMessage.id, true).enqueue()
            }

            chatClient.sendMessage(
                channelType,
                channelId,
                message.copy(showInChannel = isInThread && alsoSendToChannel.value),
            )
        }
        dismissMessageActions()
        clearData()
        sendMessageCall.enqueue(callback)
    }

    public fun buildNewMessage(
        message: String,
        attachments: List<Attachment> = emptyList(),
    ): Message {
        val activeAction = activeAction

        val currentUserId = chatClient.getCurrentUser()?.id
        val trimmedMessage = message.trim()
        val activeMessage = activeAction?.message ?: Message()
        val replyMessageId = (activeAction as? Reply)?.message?.id
        val mentions = filterMentions(selectedMentions, trimmedMessage)

        return if (isInEditMode && !activeMessage.isModerationError(currentUserId)) {
            activeMessage.copy(
                text = trimmedMessage,
                attachments = attachments.toMutableList(),
                mentionedUsersIds = mentions,
            )
        } else {
            Message(
                cid = channelCid,
                text = trimmedMessage,
                parentId = parentMessageId,
                replyMessageId = replyMessageId,
                attachments = attachments.toMutableList(),
                mentionedUsersIds = mentions,
            )
        }
    }

    private fun filterMentions(selectedMentions: Set<User>, message: String): MutableList<String> {
        val text = message.lowercase()

        val remainingMentions = selectedMentions.filter {
            text.contains("@${it.name.lowercase()}")
        }.map { it.id }

        this.selectedMentions.clear()
        return remainingMentions.toMutableList()
    }

    public fun leaveThread() {
        setMessageMode(MessageMode.Normal)
        dismissMessageActions()
    }

    public fun onCleared() {
        typingUpdatesBuffer.clear()
        audioRecordingController.onCleared()
        scope.cancel()
    }

    private fun handleValidationErrors() {
        validationErrors.value = messageValidator.validateMessage(messageInput.value.text, selectedAttachments.value)
    }

    public fun selectMention(user: User) {
        val augmentedMessageText = "${messageText.substringBeforeLast("@")}@${user.name} "

        setMessageInputInternal(augmentedMessageText, MessageInput.Source.MentionSelected)
        selectedMentions += user
    }

    public fun selectCommand(command: Command) {
        setMessageInputInternal("/${command.name} ", MessageInput.Source.CommandSelected)
    }

    public fun toggleCommandsVisibility() {
        val isHidden = commandSuggestions.value.isEmpty()

        commandSuggestions.value = if (isHidden) commands else emptyList()
    }

    public fun dismissSuggestionsPopup() {
        mentionSuggestions.value = emptyList()
        commandSuggestions.value = emptyList()
    }

    public fun startRecording(): Unit = audioRecordingController.startRecording()

    public fun lockRecording(): Unit = audioRecordingController.lockRecording()

    public fun cancelRecording(): Unit = audioRecordingController.cancelRecording()

    public fun toggleRecordingPlayback(): Unit = audioRecordingController.toggleRecordingPlayback()

    public fun stopRecording(): Unit = audioRecordingController.stopRecording()

    public fun completeRecording(): Unit = audioRecordingController.completeRecording()

    public fun pauseRecording(): Unit = audioRecordingController.pauseRecording()

    public fun seekRecordingTo(progress: Float): Unit = audioRecordingController.seekRecordingTo(progress)

    private suspend fun handleMentionSuggestions() {
        val messageInput = messageInput.value
        if (messageInput.source == MessageInput.Source.MentionSelected) {
            logger.v { "[handleMentionSuggestions] rejected (messageInput came from mention selection)" }
            mentionSuggestions.value = emptyList()
            return
        }
        val inputText = messageInput.text
        scope.launch(DispatcherProvider.IO) {
            val suggestion = mentionSuggester.typingSuggestion(inputText)
            logger.v { "[handleMentionSuggestions] suggestion: $suggestion" }
            val result = if (suggestion != null) {
                userLookupHandler.handleUserLookup(suggestion.text)
            } else {
                emptyList()
            }
            withContext(DispatcherProvider.Main) {
                mentionSuggestions.value = result
            }
        }
    }

    private fun handleCommandSuggestions() {
        val containsCommand = CommandPattern.matcher(messageText).find()

        commandSuggestions.value = if (containsCommand && selectedAttachments.value.isEmpty()) {
            val commandPattern = messageText.removePrefix("/")
            commands.filter { it.name.startsWith(commandPattern) }
        } else {
            emptyList()
        }
    }

    private fun handleLastSentMessageDate(cooldownInterval: Int, lastSentMessageDate: Date?) {
        val isSlowModeActive = cooldownInterval > 0 && isSlowModeActive.value && !isSlowModeDisabled.value

        if (isSlowModeActive && lastSentMessageDate != null && !isInEditMode) {
            val elapsedTime: Long = TimeUnit.MILLISECONDS
                .toSeconds(System.currentTimeMillis() - lastSentMessageDate.time)
                .coerceAtLeast(0)

            fun updateCooldownTime(timeRemaining: Int) {
                cooldownTimer.value = timeRemaining
                state.value = state.value.copy(coolDownTime = timeRemaining)
            }

            if (elapsedTime < cooldownInterval) {
                cooldownTimerJob?.cancel()
                cooldownTimerJob = scope.launch {
                    for (timeRemaining in cooldownInterval - elapsedTime downTo 0) {
                        updateCooldownTime(timeRemaining.toInt())
                        delay(OneSecond)
                    }
                }
            } else {
                updateCooldownTime(0)
            }
        }
    }

    private suspend fun handleLinkPreviews() {
        if (!isLinkPreviewEnabled) return
        val urls = LinkPattern.findAll(messageText).map {
            it.value
        }.toList()
        logger.v { "[handleLinkPreviews] urls: $urls" }
        val previews = urls.take(1)
            .map { url -> chatClient.enrichPreview(url).await() }
            .filterIsInstance<Result.Success<LinkPreview>>()
            .map { it.value }

        logger.v { "[handleLinkPreviews] previews: ${previews.map { it.originUrl }}" }
        linkPreviews.value = previews
    }


    private fun getEditMessageCall(message: Message): Call<Message> {
        return chatClient.updateMessage(message)
    }

    /**
     * Makes an API call signaling that a typing event has occurred.
     */
    private fun sendKeystrokeEvent() {
        val (type, id) = channelCid.cidToTypeAndId()

        chatClient.keystroke(type, id, parentMessageId).enqueue()
    }

    private fun sendStopTypingEvent() {
        val (type, id) = channelCid.cidToTypeAndId()

        chatClient.stopTyping(type, id, parentMessageId).enqueue()
    }

    private fun ChatClient.enrichPreview(url: String): Call<LinkPreview> {
        val urlWithScheme = url.addSchemeToUrlIfNeeded()
        return this.enrichUrl(urlWithScheme).map { LinkPreview(url, it) }
    }

    internal companion object {

        private const val MentionStartSymbol: String = "@"

        private val CommandPattern = Pattern.compile("^/[a-z]*$")

        internal val LinkPattern = Regex(
            "(http://|https://)?([a-zA-Z0-9]+(\\.[a-zA-Z0-9-]+)*\\.([a-zA-Z]{2,}))(/[\\w-./?%&=]*)?",
        )

        private const val OneSecond = 1000L

        private const val TEXT_INPUT_DEBOUNCE_TIME = 300L
    }
}
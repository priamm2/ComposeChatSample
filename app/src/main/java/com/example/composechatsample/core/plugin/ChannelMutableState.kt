package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.ChannelState
import com.example.composechatsample.core.events.TypingStartEvent
import com.example.composechatsample.core.getCreatedAtOrDefault
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.ChannelData
import com.example.composechatsample.core.models.ChannelUserRead
import com.example.composechatsample.core.models.Config
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.MessagesState
import com.example.composechatsample.core.models.TypingEvent
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.mapper.syncUnreadCountWithReads
import com.example.composechatsample.core.updateUsers
import com.example.composechatsample.core.wasCreatedAfter
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

class ChannelMutableState(
    override val channelType: String,
    override val channelId: String,
    private val userFlow: StateFlow<User?>,
    latestUsers: StateFlow<Map<String, User>>,
) : ChannelState {

    override val cid: String = "%s:%s".format(channelType, channelId)

    private val seq = seqGenerator.incrementAndGet()
    private val logger by taggedLogger("Chat:ChannelState-$seq")

    private var _messages: MutableStateFlow<Map<String, Message>>? = MutableStateFlow(emptyMap())
    private var _countedMessage: MutableSet<String>? = mutableSetOf()
    private var _typing: MutableStateFlow<TypingEvent>? = MutableStateFlow(TypingEvent(channelId, emptyList()))
    private var _typingChatEvents: MutableStateFlow<Map<String, TypingStartEvent>>? = MutableStateFlow(emptyMap())
    private var _rawReads: MutableStateFlow<Map<String, ChannelUserRead>>? = MutableStateFlow(emptyMap())
    private var rawReads: StateFlow<Map<String, ChannelUserRead>> = _rawReads!!
    private var _members: MutableStateFlow<Map<String, Member>>? = MutableStateFlow(emptyMap())
    private var _oldMessages: MutableStateFlow<Map<String, Message>>? = MutableStateFlow(emptyMap())
    private var _watchers: MutableStateFlow<Map<String, User>>? = MutableStateFlow(emptyMap())
    private var _watcherCount: MutableStateFlow<Int>? = MutableStateFlow(0)
    private var _endOfNewerMessages: MutableStateFlow<Boolean>? = MutableStateFlow(true)
    private var _endOfOlderMessages: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _loading: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _hidden: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _muted: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _channelData: MutableStateFlow<ChannelData?>? = MutableStateFlow(null)
    private var _repliedMessage: MutableStateFlow<Message?>? = MutableStateFlow(null)
    private var _quotedMessagesMap: MutableStateFlow<MutableMap<String, List<String>>>? =
        MutableStateFlow(mutableMapOf())
    private var _membersCount: MutableStateFlow<Int>? = MutableStateFlow(0)
    private var _insideSearch: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _loadingOlderMessages: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _loadingNewerMessages: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _lastSentMessageDate: MutableStateFlow<Date?>? = MutableStateFlow(null)

    private var _channelConfig: MutableStateFlow<Config>? = MutableStateFlow(Config())

    override val hidden: StateFlow<Boolean> = _hidden!!
    override val muted: StateFlow<Boolean> = _muted!!
    override val loading: StateFlow<Boolean> = _loading!!
    override val loadingOlderMessages: StateFlow<Boolean> = _loadingOlderMessages!!
    override val loadingNewerMessages: StateFlow<Boolean> = _loadingNewerMessages!!
    override val endOfOlderMessages: StateFlow<Boolean> = _endOfOlderMessages!!

    override val endOfNewerMessages: StateFlow<Boolean> = _endOfNewerMessages!!

    var hideMessagesBefore: Date? = null

    internal val cachedLatestMessages: MutableStateFlow<Map<String, Message>> = MutableStateFlow(emptyMap())

    val messageList: StateFlow<List<Message>> =
        combineStates(_messages!!, latestUsers) { messageMap, userMap -> messageMap.values.updateUsers(userMap) }
    private val sortedVisibleMessages: StateFlow<List<Message>> =
        messagesTransformation(messageList)

    override val messagesState: StateFlow<MessagesState> =
        combineStates(loading, sortedVisibleMessages) { loading: Boolean, messages: List<Message> ->
            when {
                loading -> MessagesState.Loading
                messages.isEmpty() -> MessagesState.OfflineNoResults
                else -> MessagesState.Result(messages)
            }
        }

    private fun messagesTransformation(messages: StateFlow<Collection<Message>>): StateFlow<List<Message>> {
        return combineStates(messages, userFlow) { messageCollection, user ->
            messageCollection.asSequence()
                .filter { it.parentId == null || it.showInChannel }
                .filter { it.user.id == user?.id || !it.shadowed }
                .filter { hideMessagesBefore == null || it.wasCreatedAfter(hideMessagesBefore) }
                .sortedBy { it.createdAt ?: it.createdLocallyAt }
                .toList()
        }
    }

    var lastStartTypingEvent: Date? = null
    internal var keystrokeParentMessageId: String? = null

    internal val visibleMessages: StateFlow<Map<String, Message>> = messageList.mapState { messages ->
        messages.filter { message -> hideMessagesBefore == null || message.wasCreatedAfter(hideMessagesBefore) }
            .associateBy(Message::id)
    }

    val sortedMessages: StateFlow<List<Message>> = visibleMessages.mapState { messagesMap ->
        messagesMap.values.sortedBy { message -> message.createdAt ?: message.createdLocallyAt }
    }

    override val repliedMessage: StateFlow<Message?> = _repliedMessage!!

    override val quotedMessagesMap: StateFlow<Map<String, List<String>>> = _quotedMessagesMap!!

    override val channelConfig: StateFlow<Config> = _channelConfig!!

    override val messages: StateFlow<List<Message>> = sortedVisibleMessages

    override val oldMessages: StateFlow<List<Message>> = messagesTransformation(_oldMessages!!.mapState { it.values })
    override val watcherCount: StateFlow<Int> = _watcherCount!!

    override val watchers: StateFlow<List<User>> =
        combineStates(_watchers!!, latestUsers) { watcherMap, userMap -> watcherMap.values.updateUsers(userMap) }
            .mapState { it.sortedBy(User::createdAt) }

    override val typing: StateFlow<TypingEvent> = _typing!!

    override val reads: StateFlow<List<ChannelUserRead>> = rawReads
        .mapState { it.values.sortedBy(ChannelUserRead::lastRead) }

    override val read: StateFlow<ChannelUserRead?> =
        combineStates(rawReads, userFlow) { readsMap, user -> user?.id?.let { readsMap[it] } }

    val lastMarkReadEvent: StateFlow<Date?> = read.mapState { it?.lastRead }

    override val unreadCount: StateFlow<Int> = read.mapState { it?.unreadMessages ?: 0 }

    override val members: StateFlow<List<Member>> =
        combineStates(_members!!, latestUsers) { membersMap, usersMap -> membersMap.values.updateUsers(usersMap) }
            .mapState { it.sortedBy(Member::createdAt) }

    override val membersCount: StateFlow<Int> = _membersCount!!

    override val channelData: StateFlow<ChannelData> =
        combineStates(_channelData!!, latestUsers) { channelData, users ->
            if (channelData == null) {
                ChannelData(
                    type = channelType,
                    id = channelId,
                )
            } else {
                val result = if (users.containsKey(channelData.createdBy.id)) {
                    channelData.copy(createdBy = users[channelData.createdBy.id] ?: channelData.createdBy)
                } else {
                    channelData
                }
                result
            }
        }

    override var recoveryNeeded: Boolean = false

    override val insideSearch: StateFlow<Boolean> = _insideSearch!!

    override val lastSentMessageDate: StateFlow<Date?> = _lastSentMessageDate!!

    override fun toChannel(): Channel {
        val channelData = channelData.value

        val messages = sortedMessages.value
        val cachedMessages = cachedLatestMessages.value.values.toList()
        val members = members.value
        val watchers = watchers.value
        val reads = rawReads.value.values.toList()
        val watcherCount = watcherCount.value
        val insideSearch = insideSearch.value

        val channel = channelData
            .toChannel(messages, cachedMessages, members, reads, watchers, watcherCount, insideSearch)
        return channel.copy(
            config = channelConfig.value,
            hidden = hidden.value,
            isInsideSearch = insideSearch,
            cachedLatestMessages = cachedLatestMessages.value.values.toList(),
        ).syncUnreadCountWithReads()
    }

    fun setLoadingOlderMessages(isLoading: Boolean) {
        _loadingOlderMessages?.value = isLoading
    }

    fun setLoadingNewerMessages(isLoading: Boolean) {
        _loadingNewerMessages?.value = isLoading
    }

    fun setEndOfNewerMessages(isEnd: Boolean) {
        _endOfNewerMessages?.value = isEnd
    }

    fun setEndOfOlderMessages(isEnd: Boolean) {
        _endOfOlderMessages?.value = isEnd
    }

    fun setLoading(isLoading: Boolean) {
        _loading?.value = isLoading
    }

    fun setHidden(isHidden: Boolean) {
        _hidden?.value = isHidden
    }

    fun setMuted(isMuted: Boolean) {
        _muted?.value = isMuted
    }

    fun setChannelData(channelData: ChannelData) {
        _channelData?.value = channelData
    }

    fun setRepliedMessage(repliedMessage: Message?) {
        _repliedMessage?.value = repliedMessage
    }

    fun setMembersCount(count: Int) {
        _membersCount?.value = count
    }

    fun setInsideSearch(isInsideSearch: Boolean) {
        when {
            isInsideSearch && !insideSearch.value -> {
                cacheLatestMessages()
            }

            !isInsideSearch && insideSearch.value -> {
                cachedLatestMessages.value = emptyMap()
            }
        }

        _insideSearch?.value = isInsideSearch
    }

    fun setLastSentMessageDate(lastSentMessageDate: Date?) {
        _lastSentMessageDate?.value = lastSentMessageDate
    }

    fun setChannelConfig(channelConfig: Config) {
        _channelConfig?.value = channelConfig
    }

    fun addQuotedMessage(quotedMessageId: String, quotingMessageId: String) {
        _quotedMessagesMap?.apply {
            val quotesMap = value
            quotesMap[quotedMessageId] = quotesMap[quotedMessageId]?.plus(quotingMessageId) ?: listOf(quotingMessageId)
            value = quotesMap
        }
    }

    fun updateTypingEvents(eventsMap: Map<String, TypingStartEvent>, typingEvent: TypingEvent) {
        _typingChatEvents?.value = eventsMap
        _typing?.value = typingEvent
    }

    fun upsertMembers(members: List<Member>) {
        logger.d { "[upsertMembers] member.ids: ${members.map { it.getUserId() }}" }
        val membersMap = members.associateBy(Member::getUserId)
        _members?.apply { value = value + membersMap }
    }

    fun setMembers(members: List<Member>, membersCount: Int) {
        logger.d { "[setMembers] member.ids: ${members.map { it.getUserId() }}" }
        _members?.value = members.associateBy(Member::getUserId)
        _membersCount?.value = membersCount
    }

    fun addMember(member: Member) {
        logger.d { "[addMember] member.id: ${member.getUserId()}" }
        _membersCount?.value = membersCount.value +
            (1.takeUnless { _members?.value?.keys?.contains(member.getUserId()) == true } ?: 0)
        upsertMembers(listOf(member))
    }

    fun deleteMember(member: Member) {
        logger.d { "[deleteMember] member.id: ${member.getUserId()}" }
        _members?.let {
            _membersCount?.value = membersCount.value - it.value.count { it.key == member.getUserId() }
            it.value = it.value - member.getUserId()
        }
        _watchers?.let {
            deleteWatcher(
                member.user,
                watcherCount.value - it.value.count { it.key == member.getUserId() },
            )
        }
    }

    internal fun deleteWatcher(user: User, watchersCount: Int) {
        logger.v { "[deleteWatcher] user.id: ${user.id}, watchersCount: $watchersCount" }
        _watchers?.let { upsertWatchers((it.value - user.id).values.toList(), watchersCount) }
    }

    fun deleteMessage(message: Message, updateCount: Boolean = true) {
        _messages?.apply { value = value - message.id }

        if (updateCount) {
            _countedMessage?.remove(message.id)
        }
    }

    fun upsertWatchers(watchers: List<User>, watchersCount: Int) {
        logger.v { "[upsertWatchers] watchers.ids: ${watchers.map { it.id }}, watchersCount: $watchersCount" }
        _watchers?.apply {
            value = value + watchers.associateBy(User::id)
            _watcherCount?.value = watchersCount.takeUnless { it < 0 } ?: value.size
        }
    }

    fun setWatchers(watchers: List<User>, watchersCount: Int) {
        logger.v { "[setWatchers] watchers.ids: ${watchers.map { it.id }}, watchersCount: $watchersCount" }
        _watchers?.value = watchers.associateBy(User::id)
        _watcherCount?.value = watchersCount
    }

    fun upsertMessage(message: Message, updateCount: Boolean = true) {
        _messages?.apply { value = value + (message.id to message) }

        if (updateCount) {
            _countedMessage?.add(message.id)
        }
    }

    fun upsertUserPresence(user: User) {
        logger.d { "[upsertUserPresence] user.id: ${user.id}" }
        _members?.value?.get(user.id)?.copy(user = user)?.let { upsertMembers(listOf(it)) }
        user.takeIf { _watchers?.value?.any { it.key == user.id } == true }
            ?.let { upsertWatchers(listOf(it), watcherCount.value) }
        _channelData?.value?.takeIf { it.createdBy.id == user.id }
            ?.let { setChannelData(it.copy(createdBy = user)) }
        _messages?.apply { value = value.values.updateUsers(mapOf(user.id to user)).associateBy { it.id } }
    }

    fun upsertReads(reads: List<ChannelUserRead>) {
        _rawReads?.apply {
            value = value + reads.associateBy(ChannelUserRead::getUserId)
        }
    }

    fun markChannelAsRead(): Boolean = read.value
        ?.takeIf { channelConfig.value.readEventsEnabled }
        ?.let { currentUserRead ->
            messages.value.lastOrNull()?.let { lastMessage ->
                upsertReads(
                    listOf(
                        currentUserRead.copy(
                            lastReceivedEventDate = lastMessage.getCreatedAtOrDefault(Date()),
                            lastRead = lastMessage.getCreatedAtOrDefault(Date()),
                            unreadMessages = 0,
                        ),
                    ),
                )
                true
            }
        } ?: false

    fun removeMessagesBefore(date: Date) {
        _messages?.apply { value = value.filter { it.value.wasCreatedAfter(date) } }
    }

    fun upsertMessages(updatedMessages: Collection<Message>, updateCount: Boolean = true) {
        _messages?.apply { value += updatedMessages.associateBy(Message::id) }

        if (updateCount) {
            _countedMessage?.addAll(updatedMessages.map { it.id })
        }
    }

    fun setMessages(messages: List<Message>) {
        _messages?.value = messages.associateBy(Message::id)
    }

    private fun cacheLatestMessages() {
        cachedLatestMessages.value = sortedMessages.value.associateBy(Message::id)
    }

    fun updateCachedLatestMessages(messages: Map<String, Message>) {
        cachedLatestMessages.value = messages
    }

    fun clearCountedMessages() {
        _countedMessage?.clear()
    }

    fun insertCountedMessages(ids: List<String>) {
        _countedMessage?.addAll(ids)
    }

    override fun getMessageById(id: String): Message? = _messages?.value?.get(id)

    internal fun destroy() {
        _messages = null
        _countedMessage = null
        _typing = null
        _typingChatEvents = null
        _rawReads = null
        _members = null
        _oldMessages = null
        _watchers = null
        _watcherCount = null
        _endOfNewerMessages = null
        _endOfOlderMessages = null
        _loading = null
        _hidden = null
        _muted = null
        _channelData = null
        _repliedMessage = null
        _quotedMessagesMap = null
        _membersCount = null
        _insideSearch = null
        _loadingOlderMessages = null
        _loadingNewerMessages = null
        _lastSentMessageDate = null
        _channelConfig = null
    }

    private companion object {
        private const val OFFSET_EVENT_TIME = 5L
        private val seqGenerator = AtomicInteger()
    }
}
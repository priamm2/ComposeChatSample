package com.example.composechatsample.core.plugin

import androidx.collection.LruCache
import com.example.composechatsample.core.ChannelMessagesUpdateLogic
import com.example.composechatsample.core.ChannelState
import com.example.composechatsample.core.NEVER
import com.example.composechatsample.core.api.QueryChannelRequest
import com.example.composechatsample.core.events.TypingStartEvent
import com.example.composechatsample.core.events.UserStartWatchingEvent
import com.example.composechatsample.core.events.UserStopWatchingEvent
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.ChannelData
import com.example.composechatsample.core.models.ChannelUserRead
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.models.TypingEvent
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.state.ClientState
import com.example.composechatsample.log.StreamLog
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.isPermanent
import com.example.composechatsample.core.isReply
import java.util.Date

class ChannelStateLogic(
    private val clientState: ClientState,
    private val mutableState: ChannelMutableState,
    private val globalMutableState: MutableGlobalState,
    private val searchLogic: SearchLogic,
    private val attachmentUrlValidator: AttachmentUrlValidator = AttachmentUrlValidator(),
    coroutineScope: CoroutineScope,
) : ChannelMessagesUpdateLogic {

    private val logger by taggedLogger(TAG)
    private val processedMessageIds = LruCache<String, Boolean>(CACHE_SIZE)

    private val typingEventPruner = TypingEventPruner(
        coroutineScope = coroutineScope,
        channelId = mutableState.channelId,
        onUpdated = ::updateTypingStates,
    )

    override fun listenForChannelState(): ChannelState {
        return mutableState
    }

    fun writeChannelState(): ChannelMutableState = mutableState

    fun updateChannelData(channel: Channel) {
        val currentOwnCapabilities = mutableState.channelData.value.ownCapabilities
        mutableState.setChannelData(ChannelData(channel, currentOwnCapabilities))
    }

    private fun updateReads(reads: List<ChannelUserRead>) {
        logger.v { "[updateReads] cid: ${mutableState.cid}, reads.size: ${reads.size}" }
        mutableState.upsertReads(reads)
    }

    fun updateRead(read: ChannelUserRead) = updateReads(listOf(read))

    fun setTyping(userId: String, event: TypingStartEvent?) {
        if (userId != clientState.user.value?.id) {
            typingEventPruner.processEvent(userId, typingStartEvent = event)
        }
    }

    private fun updateTypingStates(
        rawTypingEvents: Map<String, TypingStartEvent>,
        typingEvent: TypingEvent,
    ) {
        mutableState.updateTypingEvents(eventsMap = rawTypingEvents, typingEvent = typingEvent)
        globalMutableState.tryEmitTypingEvent(cid = mutableState.cid, typingEvent = typingEvent)
    }

    private fun upsertWatchers(watchers: List<User>, watchersCount: Int) {
        mutableState.upsertWatchers(watchers, watchersCount)
    }

    fun setWatchers(watchers: List<User>, watchersCount: Int) {
        mutableState.setWatchers(watchers, watchersCount)
    }

    override fun upsertMessage(message: Message, updateCount: Boolean) {
        logger.d {
            "[upsertMessage] message.id: ${message.id}, " +
                "message.text: ${message.text}, updateCount: $updateCount"
        }
        if (mutableState.visibleMessages.value.containsKey(message.id) || !mutableState.insideSearch.value) {
            upsertMessages(listOf(message), updateCount = updateCount)
        } else {
            mutableState.updateCachedLatestMessages(parseCachedMessages(listOf(message)))
        }
    }

    override fun upsertMessages(messages: List<Message>, shouldRefreshMessages: Boolean, updateCount: Boolean) {
        val first = messages.firstOrNull()
        val last = messages.lastOrNull()
        logger.d {
            "[upsertMessages] messages.size: ${messages.size}, first: ${first?.text?.take(TEXT_LIMIT)}, " +
                "last: ${last?.text?.take(TEXT_LIMIT)}, shouldRefreshMessages: $shouldRefreshMessages, " +
                "updateCount: $updateCount"
        }
        when (shouldRefreshMessages) {
            true -> {
                messages.filter { message -> message.isReply() }.forEach(::addQuotedMessage)
                mutableState.setMessages(messages)

                if (updateCount) {
                    mutableState.clearCountedMessages()
                    mutableState.insertCountedMessages(messages.map { it.id })
                }
            }
            false -> {
                val oldMessages = mutableState.messageList.value.associateBy(Message::id)
                val newMessages = attachmentUrlValidator.updateValidAttachmentsUrl(messages, oldMessages)
                    .filter { newMessage -> isMessageNewerThanCurrent(oldMessages[newMessage.id], newMessage) }

                messages.filter { message -> message.isReply() }.forEach(::addQuotedMessage)

                val normalizedMessages =
                    newMessages.flatMap { message -> normalizeReplyMessages(message) ?: emptyList() }
                mutableState.upsertMessages(newMessages + normalizedMessages, updateCount)
            }
        }
    }

    fun setLastSentMessageDate(lastSentMessageDate: Date?) {
        mutableState.setLastSentMessageDate(lastSentMessageDate)
    }

    private fun normalizeReplyMessages(quotedMessage: Message): List<Message>? {
        return getAllReplies(quotedMessage)?.map { replyMessage ->
            replyMessage.copy(
                replyTo = quotedMessage,
                replyMessageId = quotedMessage.id,
            )
        }
    }

    public fun getAllReplies(message: Message): List<Message>? {
        return mutableState.quotedMessagesMap
            .value[message.id]
            ?.mapNotNull(mutableState::getMessageById)
    }

    fun deleteMessage(message: Message) {
        mutableState.deleteMessage(message)
    }

    fun removeMessagesBefore(date: Date, systemMessage: Message? = null) {
        mutableState.removeMessagesBefore(date)
        systemMessage?.let(mutableState::upsertMessage)
    }

    fun hideMessagesBefore(date: Date) {
        mutableState.hideMessagesBefore = date
    }

    fun upsertUserPresence(user: User) {
        mutableState.upsertUserPresence(user)
    }

    fun upsertMember(member: Member) {
        upsertMembers(listOf(member))
    }

    fun upsertMembers(members: List<Member>) {
        mutableState.upsertMembers(members)
    }

    fun setMembers(members: List<Member>, membersCount: Int) {
        mutableState.setMembers(members, membersCount)
    }

    fun deleteMember(member: Member) {
        mutableState.deleteMember(member)
    }

    fun updateMemberBanned(
        memberUserId: String?,
        banned: Boolean,
        shadow: Boolean,
    ) {
        mutableState.upsertMembers(
            mutableState.members.value.map { member ->
                when (member.user.id == memberUserId) {
                    true -> member.copy(
                        banned = banned,
                        shadowBanned = shadow,
                    )
                    false -> member
                }
            },
        )
    }


    fun deleteChannel(deleteDate: Date) {
        mutableState.setChannelData(mutableState.channelData.value.copy(deletedAt = deleteDate))
    }

    fun upsertWatcher(event: UserStartWatchingEvent) {
        upsertWatchers(listOf(event.user), event.watcherCount)
    }

    fun deleteWatcher(event: UserStopWatchingEvent) {
        mutableState.deleteWatcher(event.user, event.watcherCount)
    }

    fun toggleHidden(hidden: Boolean) {
        mutableState.setHidden(hidden)
    }

    override fun replyMessage(repliedMessage: Message?) {
        mutableState.setRepliedMessage(repliedMessage)
    }

    fun updateMute(isMuted: Boolean) {
        mutableState.setMuted(isMuted)
    }

    fun updateDataForChannel(
        channel: Channel,
        messageLimit: Int,
        shouldRefreshMessages: Boolean = false,
        scrollUpdate: Boolean = false,
        isNotificationUpdate: Boolean = false,
        isChannelsStateUpdate: Boolean = false,
        isWatchChannel: Boolean = false,
    ) {
        logger.d {
            "[updateDataForChannel] cid: ${channel.cid}, messageLimit: $messageLimit, " +
                "shouldRefreshMessages: $shouldRefreshMessages, scrollUpdate: $scrollUpdate, " +
                "isNotificationUpdate: $isNotificationUpdate, isChannelsStateUpdate: $isChannelsStateUpdate, " +
                "isWatchChannel: $isWatchChannel"
        }
        updateChannelData(channel)

        mutableState.setMembersCount(channel.memberCount)

        updateReads(channel.read)
        upsertMembers(channel.members)
        upsertWatchers(channel.watchers, channel.watcherCount)

        if (messageLimit != 0) {
            if (shouldUpsertMessages(
                    isNotificationUpdate = isNotificationUpdate,
                    isInsideSearch = mutableState.insideSearch.value,
                    isScrollUpdate = scrollUpdate,
                    shouldRefreshMessages = shouldRefreshMessages,
                    isChannelsStateUpdate = isChannelsStateUpdate,
                    isWatchChannel = isWatchChannel,
                )
            ) {
                upsertMessages(channel.messages, shouldRefreshMessages)
            } else {
                upsertCachedMessages(channel.messages)
            }
        }

        mutableState.setChannelConfig(channel.config)

        mutableState.setLoadingOlderMessages(false)
        mutableState.setLoadingNewerMessages(false)
    }

    private fun upsertCachedMessages(messages: List<Message>) {
        mutableState.updateCachedLatestMessages(parseCachedMessages(messages))
    }

    private fun parseCachedMessages(messages: List<Message>): Map<String, Message> {
        val currentMessages = mutableState.cachedLatestMessages.value
        return currentMessages + attachmentUrlValidator.updateValidAttachmentsUrl(messages, currentMessages)
            .filter { newMessage -> isMessageNewerThanCurrent(currentMessages[newMessage.id], newMessage) }
            .associateBy(Message::id)
    }

    @Suppress("LongParameterList")
    private fun shouldUpsertMessages(
        isNotificationUpdate: Boolean,
        isInsideSearch: Boolean,
        isScrollUpdate: Boolean,
        shouldRefreshMessages: Boolean,
        isChannelsStateUpdate: Boolean,
        isWatchChannel: Boolean,
    ): Boolean {
        return isWatchChannel ||
            shouldRefreshMessages ||
            isScrollUpdate ||
            (isNotificationUpdate && !isInsideSearch) ||
            (isChannelsStateUpdate && (mutableState.messages.value.isEmpty() || !isInsideSearch))
    }

    fun updateOldMessagesFromChannel(c: Channel) {
        mutableState.hideMessagesBefore = c.hiddenMessagesBefore
        updateChannelData(c)
        updateReads(c.read)
        mutableState.setMembersCount(c.memberCount)
        upsertMembers(c.members)
        upsertWatchers(c.watchers, c.watcherCount)
        upsertMessages(c.messages, false)
    }

    fun propagateChannelQuery(channel: Channel, request: QueryChannelRequest) {
        logger.d { "[propagateChannelQuery] cid: ${channel.cid}, request: $request" }
        val noMoreMessages = request.messagesLimit() > channel.messages.size
        val isNotificationUpdate = request.isNotificationUpdate

        if (!isNotificationUpdate && request.messagesLimit() != 0) {
            searchLogic.handleMessageBounds(request, noMoreMessages)
            mutableState.recoveryNeeded = false

            determinePaginationEnd(request, noMoreMessages)
        }

        updateDataForChannel(
            channel = channel,
            shouldRefreshMessages = request.shouldRefresh,
            scrollUpdate = request.isFilteringMessages(),
            isNotificationUpdate = request.isNotificationUpdate,
            messageLimit = request.messagesLimit(),
            isWatchChannel = request.isWatchChannel,
        )
    }

    private fun determinePaginationEnd(request: QueryChannelRequest, noMoreMessages: Boolean) {
        when {

            !request.isFilteringMessages() -> {
                mutableState.setEndOfOlderMessages(noMoreMessages)
                mutableState.setEndOfNewerMessages(true)
            }
            request.isFilteringAroundIdMessages() -> {
                mutableState.setEndOfOlderMessages(false)
                mutableState.setEndOfNewerMessages(false)
            }
            noMoreMessages -> if (request.isFilteringNewerMessages()) {
                mutableState.setEndOfNewerMessages(true)
            } else {
                mutableState.setEndOfOlderMessages(true)
            }
        }
    }

    fun propagateQueryError(error: Error) {
        if (error.isPermanent()) {
            StreamLog.d(TAG) {
                "Permanent failure calling channel.watch for channel ${mutableState.cid}, with error $error"
            }
        } else {
            StreamLog.d(TAG) {
                "Temporary failure calling channel.watch for channel ${mutableState.cid}. " +
                    "Marking the channel as needing recovery. Error was $error"
            }
            mutableState.recoveryNeeded = true
        }
    }

    fun refreshMuteState() {
        val cid = mutableState.cid
        val isChannelMuted = globalMutableState.channelMutes.value.any { it.channel.cid == cid }
        StreamLog.d(TAG) { "[onQueryChannelRequest] isChannelMuted: $isChannelMuted, cid: $cid" }
        updateMute(isChannelMuted)
    }

    private fun isMessageNewerThanCurrent(currentMessage: Message?, newMessage: Message): Boolean {
        return if (newMessage.syncStatus == SyncStatus.COMPLETED) {
            (currentMessage?.lastUpdateTime() ?: NEVER.time) <= newMessage.lastUpdateTime()
        } else {
            (currentMessage?.lastLocalUpdateTime() ?: NEVER.time) <= newMessage.lastLocalUpdateTime()
        }
    }

    private fun Message.lastUpdateTime(): Long = listOfNotNull(
        createdAt,
        updatedAt,
        deletedAt,
    ).map { it.time }
        .maxOrNull()
        ?: NEVER.time

    private fun Message.lastLocalUpdateTime(): Long = listOfNotNull(
        createdLocallyAt,
        updatedLocallyAt,
        deletedAt,
    ).map { it.time }
        .maxOrNull()
        ?: NEVER.time

    fun addMember(member: Member) {
        mutableState.addMember(member)
    }

    private fun addQuotedMessage(message: Message) {
        (message.replyTo?.id ?: message.replyMessageId)?.let { replyId ->
            mutableState.addQuotedMessage(replyId, message.id)
        }
    }

    fun loadingNewerMessages() {
        mutableState.setLoadingNewerMessages(true)
    }

    fun loadingOlderMessages() {
        mutableState.setLoadingOlderMessages(true)
    }

    fun updateCurrentUserRead(eventReceivedDate: Date, message: Message) {
        mutableState.read.value
            ?.takeUnless { it.lastReceivedEventDate.after(eventReceivedDate) }
            ?.takeUnless { processedMessageIds.get(message.id) == true }
            ?.takeUnless {
                message.user.id == clientState.user.value?.id ||
                    message.parentId?.takeUnless { message.showInChannel } != null
            }
            ?.takeUnless { message.shadowed }
            ?.let {
                updateRead(
                    it.copy(
                        lastReceivedEventDate = eventReceivedDate,
                        unreadMessages = it.unreadMessages.inc(),
                    ),
                )
            }
        processedMessageIds.put(message.id, true)
    }

    private companion object {
        private const val TAG = "Chat:ChannelStateLogic"
        private const val TEXT_LIMIT = 10
        private const val CACHE_SIZE = 100
    }
}
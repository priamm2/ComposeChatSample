package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.ChannelState
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.Pagination
import com.example.composechatsample.core.api.QueryChannelRequest
import com.example.composechatsample.core.events.ChannelDeletedEvent
import com.example.composechatsample.core.events.ChannelHiddenEvent
import com.example.composechatsample.core.events.ChannelTruncatedEvent
import com.example.composechatsample.core.events.ChannelUpdatedByUserEvent
import com.example.composechatsample.core.events.ChannelUpdatedEvent
import com.example.composechatsample.core.events.ChannelUserBannedEvent
import com.example.composechatsample.core.events.ChannelUserUnbannedEvent
import com.example.composechatsample.core.events.ChannelVisibleEvent
import com.example.composechatsample.core.events.ChatEvent
import com.example.composechatsample.core.events.ConnectedEvent
import com.example.composechatsample.core.events.ConnectingEvent
import com.example.composechatsample.core.events.ConnectionErrorEvent
import com.example.composechatsample.core.events.DisconnectedEvent
import com.example.composechatsample.core.events.ErrorEvent
import com.example.composechatsample.core.events.GlobalUserBannedEvent
import com.example.composechatsample.core.events.GlobalUserUnbannedEvent
import com.example.composechatsample.core.events.HealthEvent
import com.example.composechatsample.core.events.MarkAllReadEvent
import com.example.composechatsample.core.events.MemberAddedEvent
import com.example.composechatsample.core.events.MemberRemovedEvent
import com.example.composechatsample.core.events.MemberUpdatedEvent
import com.example.composechatsample.core.events.MessageDeletedEvent
import com.example.composechatsample.core.events.MessageReadEvent
import com.example.composechatsample.core.events.MessageUpdatedEvent
import com.example.composechatsample.core.events.NewMessageEvent
import com.example.composechatsample.core.events.NotificationAddedToChannelEvent
import com.example.composechatsample.core.events.NotificationChannelDeletedEvent
import com.example.composechatsample.core.events.NotificationChannelMutesUpdatedEvent
import com.example.composechatsample.core.events.NotificationChannelTruncatedEvent
import com.example.composechatsample.core.events.NotificationInviteAcceptedEvent
import com.example.composechatsample.core.events.NotificationInviteRejectedEvent
import com.example.composechatsample.core.events.NotificationInvitedEvent
import com.example.composechatsample.core.events.NotificationMarkReadEvent
import com.example.composechatsample.core.events.NotificationMarkUnreadEvent
import com.example.composechatsample.core.events.NotificationMessageNewEvent
import com.example.composechatsample.core.events.NotificationMutesUpdatedEvent
import com.example.composechatsample.core.events.NotificationRemovedFromChannelEvent
import com.example.composechatsample.core.events.ReactionDeletedEvent
import com.example.composechatsample.core.events.ReactionNewEvent
import com.example.composechatsample.core.events.ReactionUpdateEvent
import com.example.composechatsample.core.events.TypingStartEvent
import com.example.composechatsample.core.events.TypingStopEvent
import com.example.composechatsample.core.events.UnknownEvent
import com.example.composechatsample.core.events.UserDeletedEvent
import com.example.composechatsample.core.events.UserPresenceChangedEvent
import com.example.composechatsample.core.events.UserStartWatchingEvent
import com.example.composechatsample.core.events.UserStopWatchingEvent
import com.example.composechatsample.core.events.UserUpdatedEvent
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.repository.RepositoryFacade
import com.example.composechatsample.log.taggedLogger
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.NEVER
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.WatchChannelRequest
import com.example.composechatsample.core.api.AnyChannelPaginationRequest
import com.example.composechatsample.core.api.QueryChannelPaginationRequest
import com.example.composechatsample.core.applyPagination
import com.example.composechatsample.core.getCreatedAtOrDefault
import com.example.composechatsample.core.getCreatedAtOrNull
import com.example.composechatsample.core.toAnyChannelPaginationRequest
import com.example.composechatsample.core.toChannelUserRead
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date

class ChannelLogic(
    private val repos: RepositoryFacade,
    private val userPresence: Boolean,
    private val channelStateLogic: ChannelStateLogic,
    private val coroutineScope: CoroutineScope,
    private val getCurrentUserId: () -> String?,
) {

    private val mutableState: ChannelMutableState = channelStateLogic.writeChannelState()
    private val logger by taggedLogger("Chat:ChannelLogicDB")

    val cid: String
        get() = mutableState.cid

    suspend fun updateStateFromDatabase(request: QueryChannelRequest) {
        logger.d { "[updateStateFromDatabase] request: $request" }
        if (request.isNotificationUpdate) return
        channelStateLogic.refreshMuteState()
        if (!request.isFilteringNewerMessages()) {
            runChannelQueryOffline(request)
        }
    }

    internal fun state(): ChannelState = mutableState

    internal fun stateLogic(): ChannelStateLogic {
        return channelStateLogic
    }

    internal suspend fun watch(messagesLimit: Int = 30, userPresence: Boolean): Result<Channel> {
        logger.i { "[watch] messagesLimit: $messagesLimit, userPresence: $userPresence" }
        if (mutableState.loading.value) {
            logger.i { "Another request to watch this channel is in progress. Ignoring this request." }
            return Result.Failure(
                Error.GenericError(
                    "Another request to watch this channel is in progress. Ignoring this request.",
                ),
            )
        }
        return runChannelQuery(
            "watch",
            QueryChannelPaginationRequest(messagesLimit).toWatchChannelRequest(userPresence).apply {
                shouldRefresh = true
            },
        )
    }

    internal suspend fun loadNewerMessages(messageId: String, limit: Int): Result<Channel> {
        logger.i { "[loadNewerMessages] messageId: $messageId, limit: $limit" }
        channelStateLogic.loadingNewerMessages()
        return runChannelQuery("loadNewerMessages", newerWatchChannelRequest(limit = limit, baseMessageId = messageId))
    }

    internal suspend fun loadOlderMessages(messageLimit: Int, baseMessageId: String? = null): Result<Channel> {
        logger.i { "[loadOlderMessages] messageLimit: $messageLimit, baseMessageId: $baseMessageId" }
        channelStateLogic.loadingOlderMessages()
        return runChannelQuery(
            "loadOlderMessages",
            olderWatchChannelRequest(limit = messageLimit, baseMessageId = baseMessageId),
        )
    }

    internal suspend fun loadMessagesAroundId(aroundMessageId: String): Result<Channel> {
        logger.i { "[loadMessagesAroundId] aroundMessageId: $aroundMessageId" }
        return runChannelQuery("loadMessagesAroundId", aroundIdWatchChannelRequest(aroundMessageId))
    }

    private suspend fun runChannelQuery(
        src: String,
        request: WatchChannelRequest,
    ): Result<Channel> {
        logger.d { "[runChannelQuery] #$src; request: $request" }
        val loadedMessages = mutableState.messageList.value
        val offlineChannel = runChannelQueryOffline(request)

        val onlineResult = runChannelQueryOnline(request)
            .onSuccess { fillTheGap(request.messagesLimit(), loadedMessages, it.messages) }

        return when {
            onlineResult is Result.Success -> onlineResult
            offlineChannel != null -> Result.Success(offlineChannel)
            else -> onlineResult
        }
    }

    private suspend fun runChannelQueryOnline(request: WatchChannelRequest): Result<Channel> =
        ChatClient.instance()
            .queryChannel(mutableState.channelType, mutableState.channelId, request, skipOnRequest = true)
            .await()

    private fun fillTheGap(
        messageLimit: Int,
        loadedMessages: List<Message>,
        requestedMessages: List<Message>,
    ) {
        if (loadedMessages.isEmpty() || requestedMessages.isEmpty() || messageLimit <= 0) return
        coroutineScope.launch {
            val loadedMessageIds = loadedMessages
                .filter { it.getCreatedAtOrNull() != null }
                .sortedBy { it.getCreatedAtOrDefault(NEVER) }
                .map { it.id }
            val requestedMessageIds = requestedMessages
                .filter { it.getCreatedAtOrNull() != null }
                .sortedBy { it.getCreatedAtOrDefault(NEVER) }
                .map { it.id }
            val intersection = loadedMessageIds.intersect(requestedMessageIds.toSet())
            val loadedMessagesOlderDate = loadedMessages.minOf { it.getCreatedAtOrDefault(Date()) }
            val loadedMessagesNewerDate = loadedMessages.maxOf { it.getCreatedAtOrDefault(NEVER) }
            val requestedMessagesOlderDate = requestedMessages.minOf { it.getCreatedAtOrDefault(Date()) }
            val requestedMessagesNewerDate = requestedMessages.maxOf { it.getCreatedAtOrDefault(NEVER) }
            if (intersection.isEmpty()) {
                when {
                    loadedMessagesOlderDate > requestedMessagesNewerDate ->
                        runChannelQueryOnline(
                            newerWatchChannelRequest(
                                messageLimit,
                                requestedMessageIds.last(),
                            ),
                        )

                    loadedMessagesNewerDate < requestedMessagesOlderDate ->
                        runChannelQueryOnline(
                            olderWatchChannelRequest(
                                messageLimit,
                                requestedMessageIds.first(),
                            ),
                        )

                    else -> null
                }?.onSuccess { fillTheGap(messageLimit, loadedMessages, it.messages) }
            }
        }
    }

    private suspend fun runChannelQueryOffline(request: QueryChannelRequest): Channel? {
        if (request.isFilteringNewerMessages() || request.isFilteringAroundIdMessages()) return null

        return selectAndEnrichChannel(mutableState.cid, request)?.also { channel ->
            logger.v {
                "[runChannelQueryOffline] completed; channel.cid: ${channel.cid}, " +
                    "channel.messages.size: ${channel.messages.size}"
            }
            if (request.filteringOlderMessages()) {
                updateOldMessagesFromLocalChannel(channel)
            } else {
                updateDataFromLocalChannel(
                    localChannel = channel,
                    isNotificationUpdate = request.isNotificationUpdate,
                    messageLimit = request.messagesLimit(),
                    scrollUpdate = request.isFilteringMessages() && !request.isFilteringAroundIdMessages(),
                    shouldRefreshMessages = request.shouldRefresh,
                    isChannelsStateUpdate = true,
                )
            }
        }
    }

    private fun updateDataFromLocalChannel(
        localChannel: Channel,
        isNotificationUpdate: Boolean,
        messageLimit: Int,
        scrollUpdate: Boolean,
        shouldRefreshMessages: Boolean,
        isChannelsStateUpdate: Boolean = false,
    ) {
        logger.v {
            "[updateDataFromLocalChannel] localChannel.cid: ${localChannel.cid}, messageLimit: $messageLimit, " +
                "scrollUpdate: $scrollUpdate, shouldRefreshMessages: $shouldRefreshMessages, " +
                "isChannelsStateUpdate: $isChannelsStateUpdate"
        }
        localChannel.hidden?.let(channelStateLogic::toggleHidden)
        localChannel.hiddenMessagesBefore?.let(channelStateLogic::hideMessagesBefore)
        updateDataForChannel(
            localChannel,
            messageLimit = messageLimit,
            shouldRefreshMessages = shouldRefreshMessages,
            scrollUpdate = scrollUpdate,
            isNotificationUpdate = isNotificationUpdate,
            isChannelsStateUpdate = isChannelsStateUpdate,
        )
    }

    private fun updateOldMessagesFromLocalChannel(localChannel: Channel) {
        logger.v { "[updateOldMessagesFromLocalChannel] localChannel.cid: ${localChannel.cid}" }
        localChannel.hidden?.let(channelStateLogic::toggleHidden)
        channelStateLogic.updateOldMessagesFromChannel(localChannel)
    }

    private suspend fun selectAndEnrichChannel(
        channelId: String,
        pagination: QueryChannelRequest,
    ): Channel? = selectAndEnrichChannels(listOf(channelId), pagination.toAnyChannelPaginationRequest()).getOrNull(0)

    private suspend fun selectAndEnrichChannels(
        channelIds: List<String>,
        pagination: AnyChannelPaginationRequest,
    ): List<Channel> = repos.selectChannels(channelIds, pagination).applyPagination(pagination)

    internal fun updateDataForChannel(
        channel: Channel,
        messageLimit: Int,
        shouldRefreshMessages: Boolean = false,
        scrollUpdate: Boolean = false,
        isNotificationUpdate: Boolean = false,
        isChannelsStateUpdate: Boolean = false,
    ) {
        channelStateLogic.updateDataForChannel(
            channel,
            messageLimit,
            shouldRefreshMessages,
            scrollUpdate,
            isNotificationUpdate,
            isChannelsStateUpdate = isChannelsStateUpdate,
        )
    }

    internal fun deleteMessage(message: Message) {
        channelStateLogic.deleteMessage(message)
    }

    internal fun upsertMessage(message: Message) = channelStateLogic.upsertMessage(message)

    internal fun upsertMessages(messages: List<Message>) {
        channelStateLogic.upsertMessages(messages)
    }

    internal fun setLastSentMessageDate(lastSentMessageDate: Date?) {
        channelStateLogic.setLastSentMessageDate(lastSentMessageDate)
    }

    private fun olderWatchChannelRequest(limit: Int, baseMessageId: String?): WatchChannelRequest =
        watchChannelRequest(Pagination.LESS_THAN, limit, baseMessageId)

    private fun newerWatchChannelRequest(limit: Int, baseMessageId: String?): WatchChannelRequest =
        watchChannelRequest(Pagination.GREATER_THAN, limit, baseMessageId)

    private fun aroundIdWatchChannelRequest(aroundMessageId: String): WatchChannelRequest {
        return QueryChannelPaginationRequest().apply {
            messageFilterDirection = Pagination.AROUND_ID
            messageFilterValue = aroundMessageId
        }.toWatchChannelRequest(userPresence).apply {
            shouldRefresh = true
        }
    }

    private fun watchChannelRequest(pagination: Pagination, limit: Int, baseMessageId: String?): WatchChannelRequest {
        logger.d { "[watchChannelRequest] pagination: $pagination, limit: $limit, baseMessageId: $baseMessageId" }
        val messageId = baseMessageId ?: getLoadMoreBaseMessage(pagination)?.also {
            logger.v { "[watchChannelRequest] baseMessage(${it.id}): ${it.text}" }
        }?.id
        return QueryChannelPaginationRequest(limit).apply {
            messageId?.let {
                messageFilterDirection = pagination
                messageFilterValue = it
            }
        }.toWatchChannelRequest(userPresence)
    }

    private fun getLoadMoreBaseMessage(direction: Pagination): Message? {
        val messages = mutableState.sortedMessages.value.takeUnless(Collection<Message>::isEmpty) ?: return null
        return when (direction) {
            Pagination.GREATER_THAN_OR_EQUAL,
            Pagination.GREATER_THAN,
            -> messages.last()
            Pagination.LESS_THAN,
            Pagination.LESS_THAN_OR_EQUAL,
            Pagination.AROUND_ID,
            -> messages.first()
        }
    }

    private fun removeMessagesBefore(date: Date, systemMessage: Message? = null) {
        channelStateLogic.removeMessagesBefore(date, systemMessage)
    }

    internal fun hideMessagesBefore(date: Date) {
        channelStateLogic.hideMessagesBefore(date)
    }

    private fun upsertEventMessage(message: Message) {
        channelStateLogic.upsertMessage(
            message.copy(ownReactions = getMessage(message.id)?.ownReactions ?: message.ownReactions),
            updateCount = false,
        )
    }

    internal fun getMessage(messageId: String): Message? =
        mutableState.visibleMessages.value[messageId]?.copy()

    private fun upsertUserPresence(user: User) {
        channelStateLogic.upsertUserPresence(user)
    }

    private fun upsertUser(user: User) {
        upsertUserPresence(user)
    }

    internal fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            handleEvent(event)
        }
    }


    internal fun handleEvent(event: ChatEvent) {
        val currentUserId = getCurrentUserId()
        logger.d { "[handleEvent] cid: $cid, currentUserId: $currentUserId, event: $event" }
        when (event) {
            is NewMessageEvent -> {
                upsertEventMessage(event.message)
                channelStateLogic.updateCurrentUserRead(event.createdAt, event.message)
                channelStateLogic.takeUnless { event.message.shadowed }?.toggleHidden(false)
            }
            is MessageUpdatedEvent -> {
                event.message.copy(
                    replyTo = event.message.replyMessageId
                        ?.let { mutableState.getMessageById(it) }
                        ?: event.message.replyTo,
                ).let(::upsertEventMessage)

                channelStateLogic.toggleHidden(false)
            }
            is MessageDeletedEvent -> {
                if (event.hardDelete) {
                    deleteMessage(event.message)
                } else {
                    upsertEventMessage(event.message)
                }
                channelStateLogic.toggleHidden(false)
            }
            is NotificationMessageNewEvent -> {
                if (!mutableState.insideSearch.value) {
                    upsertEventMessage(event.message)
                }
                channelStateLogic.updateCurrentUserRead(event.createdAt, event.message)
                channelStateLogic.toggleHidden(false)
            }
            is ReactionNewEvent -> {
                upsertEventMessage(event.message)
            }
            is ReactionUpdateEvent -> {
                upsertEventMessage(event.message)
            }
            is ReactionDeletedEvent -> {
                upsertEventMessage(event.message)
            }
            is MemberRemovedEvent -> {
                if (event.user.id == currentUserId) {
                    logger.i { "[handleEvent] skip MemberRemovedEvent for currentUser" }
                    return
                }
                channelStateLogic.deleteMember(event.member)
            }
            is NotificationRemovedFromChannelEvent -> {
                channelStateLogic.setMembers(event.channel.members, event.channel.memberCount)
                channelStateLogic.setWatchers(event.channel.watchers, event.channel.watcherCount)
            }
            is MemberAddedEvent -> {
                channelStateLogic.addMember(event.member)
            }
            is MemberUpdatedEvent -> {
                channelStateLogic.upsertMember(event.member)
            }
            is NotificationAddedToChannelEvent -> {
                channelStateLogic.upsertMembers(event.channel.members)
            }
            is UserPresenceChangedEvent -> {
                upsertUserPresence(event.user)
            }
            is UserUpdatedEvent -> {
                upsertUser(event.user)
            }
            is UserStartWatchingEvent -> {
                channelStateLogic.upsertWatcher(event)
            }
            is UserStopWatchingEvent -> {
                channelStateLogic.deleteWatcher(event)
            }
            is ChannelUpdatedEvent -> {
                channelStateLogic.updateChannelData(event.channel)
            }
            is ChannelUpdatedByUserEvent -> {
                channelStateLogic.updateChannelData(event.channel)
            }
            is ChannelHiddenEvent -> {
                channelStateLogic.toggleHidden(true)
            }
            is ChannelVisibleEvent -> {
                channelStateLogic.toggleHidden(false)
            }
            is ChannelDeletedEvent -> {
                removeMessagesBefore(event.createdAt)
                channelStateLogic.deleteChannel(event.createdAt)
            }
            is ChannelTruncatedEvent -> {
                removeMessagesBefore(event.createdAt, event.message)
            }
            is NotificationChannelTruncatedEvent -> {
                removeMessagesBefore(event.createdAt)
            }
            is TypingStopEvent -> {
                channelStateLogic.setTyping(event.user.id, null)
            }
            is TypingStartEvent -> {
                channelStateLogic.setTyping(event.user.id, event)
            }
            is MessageReadEvent -> {
                channelStateLogic.updateRead(event.toChannelUserRead())
            }
            is NotificationMarkReadEvent -> {
                channelStateLogic.updateRead(event.toChannelUserRead())
            }
            is MarkAllReadEvent -> {
                channelStateLogic.updateRead(event.toChannelUserRead())
            }
            is NotificationMarkUnreadEvent -> {
                channelStateLogic.updateRead(event.toChannelUserRead())
            }
            is NotificationInviteAcceptedEvent -> {
                channelStateLogic.addMember(event.member)
                channelStateLogic.updateChannelData(event.channel)
            }
            is NotificationInviteRejectedEvent -> {
                channelStateLogic.deleteMember(event.member)
                channelStateLogic.updateChannelData(event.channel)
            }
            is NotificationChannelMutesUpdatedEvent -> {
                event.me.channelMutes.any { mute ->
                    mute.channel.cid == mutableState.cid
                }.let(channelStateLogic::updateMute)
            }
            is ChannelUserBannedEvent -> {
                channelStateLogic.updateMemberBanned(
                    memberUserId = event.user.id,
                    banned = true,
                    shadow = event.shadow,
                )
            }
            is ChannelUserUnbannedEvent -> {
                channelStateLogic.updateMemberBanned(
                    memberUserId = event.user.id,
                    banned = false,
                    shadow = false,
                )
            }
            is NotificationChannelDeletedEvent,
            is NotificationInvitedEvent,
            is ConnectedEvent,
            is ConnectionErrorEvent,
            is ConnectingEvent,
            is DisconnectedEvent,
            is ErrorEvent,
            is GlobalUserBannedEvent,
            is GlobalUserUnbannedEvent,
            is HealthEvent,
            is NotificationMutesUpdatedEvent,
            is UnknownEvent,
            is UserDeletedEvent,
            -> Unit
        }
    }

    fun toChannel(): Channel = mutableState.toChannel()

    internal fun replyMessage(repliedMessage: Message?) {
        channelStateLogic.replyMessage(repliedMessage)
    }
}
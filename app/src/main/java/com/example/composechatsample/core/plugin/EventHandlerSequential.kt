package com.example.composechatsample.core.plugin

import androidx.annotation.VisibleForTesting
import com.example.composechatsample.core.ChatEventListener
import com.example.composechatsample.core.Disposable
import com.example.composechatsample.core.addMember
import com.example.composechatsample.core.addMembership
import com.example.composechatsample.core.cidToTypeAndId
import com.example.composechatsample.core.enrichIfNeeded
import com.example.composechatsample.core.events.ChannelDeletedEvent
import com.example.composechatsample.core.events.ChannelHiddenEvent
import com.example.composechatsample.core.events.ChannelTruncatedEvent
import com.example.composechatsample.core.events.ChannelUpdatedByUserEvent
import com.example.composechatsample.core.events.ChannelUpdatedEvent
import com.example.composechatsample.core.events.ChannelUserBannedEvent
import com.example.composechatsample.core.events.ChannelUserUnbannedEvent
import com.example.composechatsample.core.events.ChannelVisibleEvent
import com.example.composechatsample.core.events.ChatEvent
import com.example.composechatsample.core.events.CidEvent
import com.example.composechatsample.core.events.ConnectedEvent
import com.example.composechatsample.core.events.GlobalUserBannedEvent
import com.example.composechatsample.core.events.GlobalUserUnbannedEvent
import com.example.composechatsample.core.events.HasMessage
import com.example.composechatsample.core.events.HasOwnUser
import com.example.composechatsample.core.events.HasUnreadCounts
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
import com.example.composechatsample.core.events.UserEvent
import com.example.composechatsample.core.events.UserPresenceChangedEvent
import com.example.composechatsample.core.events.UserStartWatchingEvent
import com.example.composechatsample.core.events.UserStopWatchingEvent
import com.example.composechatsample.core.events.UserUpdatedEvent
import com.example.composechatsample.core.mergePartially
import com.example.composechatsample.core.mergeReactions
import com.example.composechatsample.core.models.ChannelCapabilities
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.UserId
import com.example.composechatsample.core.models.mergeChannelFromEvent
import com.example.composechatsample.core.parameterizedLazy
import com.example.composechatsample.core.realType
import com.example.composechatsample.core.removeMember
import com.example.composechatsample.core.removeMembership
import com.example.composechatsample.core.repository.RepositoryFacade
import com.example.composechatsample.core.repository.SocketEventCollector
import com.example.composechatsample.core.state.ClientState
import com.example.composechatsample.core.state.StateRegistry
import com.example.composechatsample.core.toChannelUserRead
import com.example.composechatsample.core.updateMember
import com.example.composechatsample.core.updateMemberBanned
import com.example.composechatsample.core.updateMembership
import com.example.composechatsample.core.updateMembershipBanned
import com.example.composechatsample.core.updateReads
import com.example.composechatsample.log.StreamLog
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.InputMismatchException
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "Chat:EventHandlerSeq"
private const val TAG_SOCKET = "Chat:SocketEvent"

internal class EventHandlerSequential(
    private val currentUserId: UserId,
    private val subscribeForEvents: (ChatEventListener<ChatEvent>) -> Disposable,
    private val logicRegistry: LogicRegistry,
    private val stateRegistry: StateRegistry,
    private val clientState: ClientState,
    private val mutableGlobalState: MutableGlobalState,
    private val repos: RepositoryFacade,
    private val sideEffect: suspend () -> Unit,
    private val syncedEvents: Flow<List<ChatEvent>>,
    scope: CoroutineScope,
) : EventHandler {

    private val logger by taggedLogger(TAG)
    private val scope = scope + SupervisorJob() + CoroutineExceptionHandler { context, throwable ->
        logger.e(throwable) { "[uncaughtCoroutineException] throwable: $throwable, context: $context" }
    }

    private val mutex = Mutex()
    private val socketEvents = MutableSharedFlow<ChatEvent>(extraBufferCapacity = Int.MAX_VALUE)
    private val socketEventCollector = SocketEventCollector(scope) { batchEvent ->
        handleBatchEvent(batchEvent)
    }

    private var eventsDisposable: Disposable = EMPTY_DISPOSABLE

    init {
        logger.d { "<init> no args" }
    }

    private val emittedCount = AtomicInteger()
    private val collectedCount = AtomicInteger()

    override fun startListening() {
        val isDisposed = eventsDisposable.isDisposed
        logger.i { "[startListening] isDisposed: $isDisposed, currentUserId: $currentUserId" }
        if (isDisposed) {
            val initJob = scope.launch {
                repos.cacheChannelConfigs()
                logger.v { "[startListening] initialization completed" }
            }
            scope.launch {
                syncedEvents.collect {
                    logger.i { "[onSyncEventsReceived] events.size: ${it.size}" }
                    handleBatchEvent(
                        BatchEvent(sortedEvents = it, isFromHistorySync = true),
                    )
                }
            }
            scope.launch {
                socketEvents.collect { event ->
                    collectedCount.incrementAndGet()
                    initJob.join()
                    sideEffect()
                    socketEventCollector.collect(event)
                }
            }
            eventsDisposable = subscribeForEvents { event ->
                if (socketEvents.tryEmit(event)) {
                    val cCount = collectedCount.get()
                    val eCount = emittedCount.incrementAndGet()
                    val ratio = eCount.toDouble() / cCount.toDouble()
                    StreamLog.v(TAG_SOCKET) {
                        "[onSocketEventReceived] event.type: ${event.realType}; $eCount => $cCount ($ratio)"
                    }
                } else {
                    StreamLog.e(TAG_SOCKET) { "[onSocketEventReceived] failed to emit socket event: $event" }
                }
            }
        }
    }

    override fun stopListening() {
        logger.i { "[stopListening] no args" }
        eventsDisposable.dispose()
        scope.coroutineContext.job.cancelChildren()
    }

    private suspend fun handleChatEvents(batchEvent: BatchEvent, queryChannelsLogic: QueryChannelsLogic) {
        logger.v { "[handleChatEvents] batchId: ${batchEvent.id}, batchEvent.size: ${batchEvent.size}" }
        queryChannelsLogic.parseChatEventResults(batchEvent.sortedEvents).forEach { result ->
            when (result) {
                is EventHandlingResult.Add -> queryChannelsLogic.addChannel(result.channel)
                is EventHandlingResult.WatchAndAdd -> queryChannelsLogic.watchAndAddChannel(result.cid)
                is EventHandlingResult.Remove -> queryChannelsLogic.removeChannel(result.cid)
                is EventHandlingResult.Skip -> Unit
            }
        }

        val hasMarkAllReadEvent = batchEvent.sortedEvents.lastOrNull { it is MarkAllReadEvent } != null
        if (hasMarkAllReadEvent) {
            queryChannelsLogic.refreshAllChannelsState()
        }

        val cids = batchEvent.sortedEvents
            .filterIsInstance<CidEvent>()
            .filterNot { it is UserStartWatchingEvent || it is UserStopWatchingEvent }
            .map { it.cid }
            .distinct()
        if (cids.isNotEmpty()) {
            queryChannelsLogic.refreshChannelsState(cids)
        }

        val event = batchEvent.sortedEvents.filterIsInstance<UserPresenceChangedEvent>().lastOrNull()
        if (event is UserPresenceChangedEvent) {
            queryChannelsLogic.refreshMembersStateForUser(event.user)
        }

        logger.v { "[handleChatEvents] completed batchId: ${batchEvent.id}" }
    }


    @VisibleForTesting
    override suspend fun handleEvents(vararg events: ChatEvent) {
        val batchEvent = BatchEvent(sortedEvents = events.toList(), isFromHistorySync = false)
        handleBatchEvent(batchEvent)
    }

    private suspend fun handleBatchEvent(event: BatchEvent) = mutex.withLock {
        try {
            logger.d {
                "[handleBatchEvent] >>> id: ${event.id}, fromSocket: ${event.isFromSocketConnection}" +
                    ", size: ${event.size}, event.types: '${event.sortedEvents.joinToString { it.realType }}'"
            }
            updateGlobalState(event)
            updateChannelsState(event)
            updateOfflineStorage(event)
            updateThreadState(event)
            logger.v { "[handleBatchEvent] <<< id: ${event.id}" }
        } catch (e: Throwable) {
            logger.e(e) { "[handleBatchEvent] failed(${event.id}): ${e.message}" }
        }
    }

    @SuppressWarnings("LongMethod", "NestedBlockDepth")
    private suspend fun updateGlobalState(batchEvent: BatchEvent) {
        logger.v { "[updateGlobalState] batchId: ${batchEvent.id}, batchEvent.size: ${batchEvent.size}" }

        var me = clientState.user.value
        var totalUnreadCount = mutableGlobalState.totalUnreadCount.value
        var channelUnreadCount = mutableGlobalState.channelUnreadCount.value

        val modifyValuesFromEvent = { event: HasUnreadCounts ->
            totalUnreadCount = event.totalUnreadCount
            channelUnreadCount = event.unreadChannels
        }

        val modifyValuesFromUser = { user: User ->
            me = user
            totalUnreadCount = user.totalUnreadCount
            channelUnreadCount = user.unreadChannels
        }

        val hasReadEventsCapability = parameterizedLazy<String, Boolean> { cid ->
            repos.hasReadEventsCapability(cid)
        }

        batchEvent.sortedEvents.forEach { event: ChatEvent ->
            when (event) {
                is ConnectedEvent -> if (batchEvent.isFromSocketConnection && event.me.id == currentUserId) {
                    modifyValuesFromUser(event.me)
                }
                is NotificationMutesUpdatedEvent -> if (event.me.id == currentUserId) {
                    modifyValuesFromUser(event.me)
                }
                is NotificationChannelMutesUpdatedEvent -> if (event.me.id == currentUserId) {
                    modifyValuesFromUser(event.me)
                }
                is UserUpdatedEvent -> if (event.user.id == currentUserId) {
                    modifyValuesFromUser(me?.mergePartially(event.user) ?: event.user)
                }
                is MarkAllReadEvent -> {
                    modifyValuesFromEvent(event)
                }
                is NotificationMessageNewEvent -> if (batchEvent.isFromSocketConnection) {
                    if (hasReadEventsCapability(event.cid)) {
                        modifyValuesFromEvent(event)
                    }
                }
                is NotificationMarkReadEvent -> if (batchEvent.isFromSocketConnection) {
                    if (hasReadEventsCapability(event.cid)) {
                        modifyValuesFromEvent(event)
                    }
                }
                is NotificationMarkUnreadEvent -> if (batchEvent.isFromSocketConnection) {
                    if (hasReadEventsCapability(event.cid)) {
                        modifyValuesFromEvent(event)
                    }
                }
                is NewMessageEvent -> if (batchEvent.isFromSocketConnection) {
                    if (hasReadEventsCapability(event.cid)) {
                        modifyValuesFromEvent(event)
                    }
                }
                else -> Unit
            }
        }

        me?.let {
            mutableGlobalState.setBanned(it.isBanned)
            mutableGlobalState.setMutedUsers(it.mutes)
            mutableGlobalState.setChannelMutes(it.channelMutes)
        }
        mutableGlobalState.setTotalUnreadCount(totalUnreadCount)
        mutableGlobalState.setChannelUnreadCount(channelUnreadCount)
        logger.v { "[updateGlobalState] completed batchId: ${batchEvent.id}" }
    }

    private suspend fun updateChannelsState(batchEvent: BatchEvent) {
        val first = batchEvent.sortedEvents.firstOrNull()
        val last = batchEvent.sortedEvents.lastOrNull()
        val firstDate = first?.createdAt
        val lastDate = last?.createdAt
        val firstLessLast = firstDate?.let { lastDate?.let { firstDate < lastDate } }
        logger.v {
            "[updateChannelsState] batchId: ${batchEvent.id}, batchEvent.size: ${batchEvent.size}" +
                ", first(${first?.seq}) < last(${last?.seq}): $firstLessLast"
        }
        val sortedEvents: List<ChatEvent> = batchEvent.sortedEvents

        stateRegistry.handleBatchEvent(batchEvent)

        sortedEvents.filterIsInstance<CidEvent>()
            .groupBy { it.cid }
            .forEach { (cid, events) ->
                val (channelType, channelId) = cid.cidToTypeAndId()
                if (events.any { it is ChannelDeletedEvent || it is NotificationChannelDeletedEvent }) {
                    logicRegistry.removeChannel(channelType, channelId)
                }
                if (logicRegistry.isActiveChannel(channelType = channelType, channelId = channelId)) {
                    val channelLogic: ChannelLogic = logicRegistry.channel(
                        channelType = channelType,
                        channelId = channelId,
                    )
                    channelLogic.handleEvents(events)
                }
            }

        sortedEvents.filterIsInstance<MarkAllReadEvent>().lastOrNull()?.let { markAllRead ->
            logicRegistry.getActiveChannelsLogic().forEach { channelLogic: ChannelLogic ->
                channelLogic.handleEvent(markAllRead)
            }
        }

        sortedEvents.filterIsInstance<NotificationChannelMutesUpdatedEvent>().lastOrNull()?.let { event ->
            logicRegistry.getActiveChannelsLogic().forEach { channelLogic: ChannelLogic ->
                channelLogic.handleEvent(event)
            }
        }

        sortedEvents.find { it is UserPresenceChangedEvent }?.let { userPresenceChanged ->
            val event = userPresenceChanged as UserPresenceChangedEvent

            stateRegistry.getActiveChannelStates()
                .filter { channelState -> channelState.members.containsWithUserId(event.user.id) }
                .forEach { channelState ->
                    val channelLogic: ChannelLogic = logicRegistry.channel(
                        channelType = channelState.channelType,
                        channelId = channelState.channelId,
                    )
                    channelLogic.handleEvent(userPresenceChanged)
                }
        }

        logicRegistry.getActiveQueryChannelsLogic().map { channelsLogic ->
            scope.async {
                handleChatEvents(batchEvent, channelsLogic)
            }
        }.awaitAll()
        logger.v { "[updateChannelsState] completed batchId: ${batchEvent.id}" }
    }

    private fun updateThreadState(batchEvent: BatchEvent) {
        logger.v { "[updateThreadState] batchEvent.size: ${batchEvent.size}" }
        val sortedEvents: List<ChatEvent> = batchEvent.sortedEvents
        sortedEvents.filterIsInstance<HasMessage>()
            .groupBy { it.message.parentId ?: it.message.id }
            .filterKeys(logicRegistry::isActiveThread)
            .forEach { (messageId, events) ->
                logicRegistry.thread(messageId).handleEvents(events)
            }
        logger.v { "[updateThreadState] completed batchId: ${batchEvent.id}" }
    }

    private suspend fun updateOfflineStorage(batchEvent: BatchEvent) {
        logger.v { "[updateOfflineStorage] batchId: ${batchEvent.id}, batchEvent.size: ${batchEvent.size} " }
        val events = batchEvent.sortedEvents.map { it.enrichIfNeeded() }
        val batchBuilder = EventBatchUpdate.Builder(batchEvent.id)
        val cidEvents = events.filterIsInstance<CidEvent>()
        batchBuilder.addToFetchChannels(
            cidEvents
                .filterNot { it is ChannelDeletedEvent || it is NotificationChannelDeletedEvent }
                .map { it.cid },
        )

        batchBuilder.addToRemoveChannels(
            cidEvents
                .filter { it is ChannelDeletedEvent || it is NotificationChannelDeletedEvent }
                .map { it.cid },
        )

        val users: List<User> = events.filterIsInstance<UserEvent>().map { it.user } +
            events.filterIsInstance<HasOwnUser>().map { it.me }

        batchBuilder.addUsers(users)

        val messageIds = events.extractMessageIds()
        batchBuilder.addToFetchMessages(messageIds)
        val batch = batchBuilder.build(mutableGlobalState, repos, currentUserId)

        for (event in events) {
            when (event) {
                is ConnectedEvent -> if (batchEvent.isFromSocketConnection) {
                    event.me.id mustBe currentUserId
                    repos.insertCurrentUser(event.me)
                }
                is NewMessageEvent -> {
                    val enrichedMessage = event.message.enrichWithOwnReactions(batch, currentUserId, event.user)
                    batch.addMessageData(event.createdAt, event.cid, enrichedMessage)
                    val channel = batch.getCurrentChannel(event.cid) ?: repos.selectChannel(event.cid)
                    if (channel == null) {
                        logger.w { "[updateOfflineStorage] #new_message; (now channel found for ${event.cid})" }
                        continue
                    }
                    val updatedChannel = channel.copy(
                        hidden = channel.hidden.takeIf { enrichedMessage.shadowed } ?: false,
                        messages = channel.messages + listOf(enrichedMessage),
                    )
                    batch.addChannel(updatedChannel)
                }
                is MessageDeletedEvent -> {
                    batch.addMessageData(
                        event.createdAt,
                        event.cid,
                        event.message.enrichWithOwnReactions(batch, currentUserId, event.user),
                    )
                }
                is MessageUpdatedEvent -> {
                    batch.addMessageData(
                        event.createdAt,
                        event.cid,
                        event.message.enrichWithOwnReactions(batch, currentUserId, event.user),
                    )
                }
                is NotificationMessageNewEvent -> {
                    batch.addMessageData(event.createdAt, event.cid, event.message)
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(channel.copy(hidden = false))
                }
                is NotificationAddedToChannelEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(
                        channel.addMembership(currentUserId, event.member),
                    )
                }
                is NotificationInvitedEvent -> {
                    batch.addUser(event.user)
                    batch.addUser(event.member.user)
                }
                is NotificationInviteAcceptedEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addUser(event.user)
                    batch.addUser(event.member.user)
                    batch.addChannel(channel)
                }
                is NotificationInviteRejectedEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addUser(event.user)
                    batch.addUser(event.member.user)
                    batch.addChannel(channel)
                }
                is ChannelHiddenEvent -> {
                    batch.getCurrentChannel(event.cid)?.let {
                        val updatedChannel = it.copy(
                            hidden = true,
                            hiddenMessagesBefore = event.createdAt.takeIf { event.clearHistory },
                        )
                        batch.addChannel(updatedChannel)
                    }
                }
                is ChannelVisibleEvent -> {
                    batch.getCurrentChannel(event.cid)?.let {
                        batch.addChannel(it.copy(hidden = false))
                    }
                }
                is NotificationMutesUpdatedEvent -> {
                    event.me.id mustBe currentUserId
                    repos.insertCurrentUser(event.me)
                }

                is ReactionNewEvent -> {
                    batch.addMessage(event.message.enrichWithOwnReactions(batch, currentUserId, event.user))
                }
                is ReactionDeletedEvent -> {
                    batch.addMessage(event.message.enrichWithOwnReactions(batch, currentUserId, event.user))
                }
                is ReactionUpdateEvent -> {
                    batch.addMessage(event.message.enrichWithOwnReactions(batch, currentUserId, event.user))
                }
                is ChannelUserBannedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.updateMemberBanned(event.user.id, banned = true, event.shadow)
                                .updateMembershipBanned(event.user.id, banned = true),
                        )
                    }
                }
                is ChannelUserUnbannedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.updateMemberBanned(event.user.id, banned = false, false)
                                .updateMembershipBanned(event.user.id, banned = false),
                        )
                    }
                }
                is MemberAddedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.addMember(event.member),
                        )
                    }
                }
                is MemberUpdatedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.updateMember(event.member)
                                .updateMembership(event.member),
                        )
                    }
                }
                is MemberRemovedEvent -> {
                    if (event.user.id == currentUserId) {
                        logger.i { "[updateOfflineStorage] skip MemberRemovedEvent for currentUser" }
                        continue
                    }
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.removeMember(event.user.id)
                                .removeMembership(currentUserId),
                        )
                    }
                }
                is NotificationRemovedFromChannelEvent -> {
                    batch.getCurrentChannel(event.cid)?.let { channel ->
                        batch.addChannel(
                            channel.removeMembership(currentUserId).copy(
                                memberCount = event.channel.memberCount,
                                members = event.channel.members,
                                watcherCount = event.channel.watcherCount,
                                watchers = event.channel.watchers,
                            ),
                        )
                    }
                }
                is ChannelUpdatedEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(channel)
                }
                is ChannelUpdatedByUserEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(channel)
                }
                is ChannelDeletedEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(channel)
                }
                is ChannelTruncatedEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(channel)
                }
                is NotificationChannelDeletedEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(channel)
                }
                is NotificationChannelMutesUpdatedEvent -> {
                    event.me.id mustBe currentUserId
                    repos.insertCurrentUser(event.me)
                }
                is NotificationChannelTruncatedEvent -> {
                    val channel = batch.getCurrentChannel(event.cid)
                        ?.mergeChannelFromEvent(event.channel) ?: event.channel
                    batch.addChannel(channel)
                }


                is MessageReadEvent ->
                    batch.getCurrentChannel(event.cid)
                        ?.updateReads(event.toChannelUserRead())
                        ?.let(batch::addChannel)

                is NotificationMarkReadEvent -> {
                    batch.getCurrentChannel(event.cid)
                        ?.updateReads(event.toChannelUserRead())
                        ?.let(batch::addChannel)
                }
                is NotificationMarkUnreadEvent -> {
                    batch.getCurrentChannel(event.cid)
                        ?.updateReads(event.toChannelUserRead())
                        ?.let(batch::addChannel)
                }
                is GlobalUserBannedEvent -> {
                    batch.addUser(event.user.copy(banned = true))
                }
                is GlobalUserUnbannedEvent -> {
                    batch.addUser(event.user.copy(banned = false))
                }
                is UserUpdatedEvent -> if (event.user.id == currentUserId) {
                    repos.insertCurrentUser(event.user)
                }
                else -> Unit
            }
        }

        batch.execute()
        for (event in events) {
            when (event) {
                is NotificationChannelTruncatedEvent -> {
                    repos.deleteChannelMessagesBefore(event.cid, event.createdAt)
                }
                is ChannelTruncatedEvent -> {
                    repos.deleteChannelMessagesBefore(event.cid, event.createdAt)
                }
                is ChannelDeletedEvent -> {
                    repos.deleteChannelMessagesBefore(event.cid, event.createdAt)
                    repos.setChannelDeletedAt(event.cid, event.createdAt)
                }
                is MessageDeletedEvent -> {
                    if (event.hardDelete) {
                        repos.deleteChannelMessage(event.message)
                    }
                }
                is MemberRemovedEvent -> {
                    repos.evictChannel(event.cid)
                }
                is NotificationRemovedFromChannelEvent -> {
                    repos.evictChannel(event.cid)
                }
                else -> Unit
            }
        }

        logger.v { "[updateOfflineStorage] completed batchId: ${batchEvent.id}" }
    }

    private fun List<ChatEvent>.extractMessageIds() = mapNotNull { event ->
        when (event) {
            is ReactionNewEvent -> event.reaction.messageId
            is ReactionDeletedEvent -> event.reaction.messageId
            is MessageDeletedEvent -> event.message.id
            is MessageUpdatedEvent -> event.message.id
            is NewMessageEvent -> event.message.id
            is NotificationMessageNewEvent -> event.message.id
            is ReactionUpdateEvent -> event.message.id
            else -> null
        }
    }

    private fun StateFlow<List<Member>>.containsWithUserId(userId: String): Boolean {
        return value.find { it.user.id == userId } != null
    }


    private suspend fun RepositoryFacade.hasReadEventsCapability(cid: String): Boolean {
        return selectChannels(listOf(cid)).let { channels ->
            val channel = channels.firstOrNull()
            if (channel?.ownCapabilities?.contains(ChannelCapabilities.READ_EVENTS) == true) {
                true
            } else {
                logger.d {
                    "Skipping unread counts update for channel: $cid. ${ChannelCapabilities.READ_EVENTS} capability is missing."
                }
                false
            }
        }
    }

    private fun Message.enrichWithOwnReactions(
        batch: EventBatchUpdate,
        currentUserId: UserId,
        eventUser: User?,
    ): Message = copy(
        ownReactions = if (eventUser != null && currentUserId != eventUser.id) {
            batch.getCurrentMessage(id)?.ownReactions ?: mutableListOf()
        } else {
            mergeReactions(
                latestReactions.filter { it.userId == currentUserId },
                batch.getCurrentMessage(id)?.ownReactions ?: mutableListOf(),
            ).toMutableList()
        },
    )

    private infix fun UserId.mustBe(currentUserId: UserId?) {
        if (this != currentUserId) {
            throw InputMismatchException(
                "received connect event for user with id $this while for user configured " +
                    "has id $currentUserId. Looks like there's a problem in the user set",
            )
        }
    }

    companion object {
        val EMPTY_DISPOSABLE = object : Disposable {
            override val isDisposed: Boolean = true
            override fun dispose() = Unit
        }
    }
}
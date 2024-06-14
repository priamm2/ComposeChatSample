package com.example.composechatsample.core

import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
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
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.BannedUser
import com.example.composechatsample.core.models.BannedUsersSort
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.Filters
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Mute
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.UploadedFile
import com.example.composechatsample.core.models.querysort.QuerySortByField
import com.example.composechatsample.core.models.querysort.QuerySorter
import java.io.File
import java.util.Date

public class ChannelClient internal constructor(
    public val channelType: String,
    public val channelId: String,
    private val client: ChatClient,
) {

    public val cid: String = "$channelType:$channelId"

    public fun get(
        messageLimit: Int = 0,
        memberLimit: Int = 0,
        state: Boolean = false,
    ): Call<Channel> {
        return client.getChannel(
            cid = cid,
            messageLimit = messageLimit,
            memberLimit = memberLimit,
            state = state,
        )
    }

    @CheckResult
    public fun create(memberIds: List<String>, extraData: Map<String, Any>): Call<Channel> {
        return client.createChannel(
            channelType = channelType,
            channelId = channelId,
            memberIds = memberIds,
            extraData = extraData,
        )
    }

    public fun subscribe(listener: ChatEventListener<ChatEvent>): Disposable {
        return client.subscribe(filterRelevantEvents(listener))
    }

    public fun subscribeFor(
        vararg eventTypes: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return client.subscribeFor(*eventTypes, listener = filterRelevantEvents(listener))
    }

    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return client.subscribeFor(
            lifecycleOwner,
            *eventTypes,
            listener = filterRelevantEvents(listener),
        )
    }

    public fun subscribeFor(
        vararg eventTypes: Class<out ChatEvent>,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return client.subscribeFor(*eventTypes, listener = filterRelevantEvents(listener))
    }

    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: Class<out ChatEvent>,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return client.subscribeFor(
            lifecycleOwner,
            *eventTypes,
            listener = filterRelevantEvents(listener),
        )
    }

    public fun subscribeForSingle(
        eventType: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return client.subscribeForSingle(eventType, listener = filterRelevantEvents(listener))
    }

    public fun <T : ChatEvent> subscribeForSingle(
        eventType: Class<T>,
        listener: ChatEventListener<T>,
    ): Disposable {
        return client.subscribeForSingle(eventType, listener = filterRelevantEvents(listener))
    }

    private fun <T : ChatEvent> filterRelevantEvents(
        listener: ChatEventListener<T>,
    ): ChatEventListener<T> {
        return ChatEventListener { event: T ->
            if (isRelevantForChannel(event)) {
                listener.onEvent(event)
            }
        }
    }

    @Suppress("ComplexMethod")
    private fun isRelevantForChannel(event: ChatEvent): Boolean {
        return when (event) {
            is ChannelDeletedEvent -> event.cid == cid
            is ChannelHiddenEvent -> event.cid == cid
            is ChannelTruncatedEvent -> event.cid == cid
            is ChannelUpdatedEvent -> event.cid == cid
            is ChannelUpdatedByUserEvent -> event.cid == cid
            is ChannelVisibleEvent -> event.cid == cid
            is MemberAddedEvent -> event.cid == cid
            is MemberRemovedEvent -> event.cid == cid
            is MemberUpdatedEvent -> event.cid == cid
            is MessageDeletedEvent -> event.cid == cid
            is MessageReadEvent -> event.cid == cid
            is MessageUpdatedEvent -> event.cid == cid
            is NewMessageEvent -> event.cid == cid
            is NotificationAddedToChannelEvent -> event.cid == cid
            is NotificationChannelDeletedEvent -> event.cid == cid
            is NotificationChannelTruncatedEvent -> event.cid == cid
            is NotificationInviteAcceptedEvent -> event.cid == cid
            is NotificationInviteRejectedEvent -> event.cid == cid
            is NotificationInvitedEvent -> event.cid == cid
            is NotificationMarkReadEvent -> event.cid == cid
            is NotificationMarkUnreadEvent -> event.cid == cid
            is NotificationMessageNewEvent -> event.cid == cid
            is NotificationRemovedFromChannelEvent -> event.cid == cid
            is ReactionDeletedEvent -> event.cid == cid
            is ReactionNewEvent -> event.cid == cid
            is ReactionUpdateEvent -> event.cid == cid
            is TypingStartEvent -> event.cid == cid
            is TypingStopEvent -> event.cid == cid
            is ChannelUserBannedEvent -> event.cid == cid
            is UserStartWatchingEvent -> event.cid == cid
            is UserStopWatchingEvent -> event.cid == cid
            is ChannelUserUnbannedEvent -> event.cid == cid
            is UnknownEvent -> event.rawData["cid"] == cid
            is HealthEvent,
            is NotificationChannelMutesUpdatedEvent,
            is NotificationMutesUpdatedEvent,
            is GlobalUserBannedEvent,
            is UserDeletedEvent,
            is UserPresenceChangedEvent,
            is GlobalUserUnbannedEvent,
            is UserUpdatedEvent,
            is ConnectedEvent,
            is ConnectionErrorEvent,
            is ConnectingEvent,
            is DisconnectedEvent,
            is ErrorEvent,
            is MarkAllReadEvent,
            -> false
        }
    }

    @CheckResult
    public fun query(request: QueryChannelRequest): Call<Channel> {
        return client.queryChannel(channelType, channelId, request)
    }

    @CheckResult
    public fun watch(request: WatchChannelRequest): Call<Channel> {
        return client.queryChannel(channelType, channelId, request)
    }

    @CheckResult
    public fun watch(data: Map<String, Any>): Call<Channel> {
        val request = WatchChannelRequest()
        request.data.putAll(data)
        return watch(request)
    }

    @CheckResult
    public fun watch(): Call<Channel> {
        return client.queryChannel(channelType, channelId, WatchChannelRequest())
    }

    @CheckResult
    public fun stopWatching(): Call<Unit> {
        return client.stopWatching(channelType, channelId)
    }

    @CheckResult
    public fun getMessage(messageId: String): Call<Message> {
        return client.getMessage(messageId)
    }

    @CheckResult
    public fun updateMessage(message: Message): Call<Message> {
        return client.updateMessage(message)
    }

    @CheckResult
    @JvmOverloads
    public fun deleteMessage(messageId: String, hard: Boolean = false): Call<Message> {
        return client.deleteMessage(messageId, hard)
    }

    @CheckResult
    @JvmOverloads
    public fun sendMessage(message: Message, isRetrying: Boolean = false): Call<Message> {
        return client.sendMessage(channelType, channelId, message, isRetrying)
    }

    @CheckResult
    public fun banUser(targetId: String, reason: String?, timeout: Int?): Call<Unit> {
        return client.banUser(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
            reason = reason,
            timeout = timeout,
        )
    }

    @CheckResult
    public fun unbanUser(targetId: String): Call<Unit> {
        return client.unbanUser(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
        )
    }

    @CheckResult
    public fun shadowBanUser(targetId: String, reason: String?, timeout: Int?): Call<Unit> {
        return client.shadowBanUser(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
            reason = reason,
            timeout = timeout,
        )
    }

    @CheckResult
    public fun removeShadowBan(targetId: String): Call<Unit> {
        return client.removeShadowBan(
            targetId = targetId,
            channelType = channelType,
            channelId = channelId,
        )
    }

    @CheckResult
    @JvmOverloads
    public fun queryBannedUsers(
        filter: FilterObject? = null,
        sort: QuerySorter<BannedUsersSort> = QuerySortByField.ascByName("created_at"),
        offset: Int? = null,
        limit: Int? = null,
        createdAtAfter: Date? = null,
        createdAtAfterOrEqual: Date? = null,
        createdAtBefore: Date? = null,
        createdAtBeforeOrEqual: Date? = null,
    ): Call<List<BannedUser>> {
        val channelCidFilter = Filters.eq("channel_cid", cid)
        return client.queryBannedUsers(
            filter = filter?.let { Filters.and(channelCidFilter, it) } ?: channelCidFilter,
            sort = sort,
            offset = offset,
            limit = limit,
            createdAtAfter = createdAtAfter,
            createdAtAfterOrEqual = createdAtAfterOrEqual,
            createdAtBefore = createdAtBefore,
            createdAtBeforeOrEqual = createdAtBeforeOrEqual,
        )
    }

    @CheckResult
    public fun markMessageRead(messageId: String): Call<Unit> {
        return client.markMessageRead(channelType, channelId, messageId)
    }

    @CheckResult
    public fun markUnread(messageId: String): Call<Unit> {
        return client.markUnread(channelType, channelId, messageId)
    }

    @CheckResult
    public fun markRead(): Call<Unit> {
        return client.markRead(channelType, channelId)
    }

    @CheckResult
    public fun delete(): Call<Channel> {
        return client.deleteChannel(channelType, channelId)
    }

    @CheckResult
    public fun show(): Call<Unit> {
        return client.showChannel(channelType, channelId)
    }

    @CheckResult
    public fun hide(clearHistory: Boolean = false): Call<Unit> {
        return client.hideChannel(channelType, channelId, clearHistory)
    }

    @CheckResult
    @JvmOverloads
    public fun truncate(systemMessage: Message? = null): Call<Channel> {
        return client.truncateChannel(channelType, channelId, systemMessage)
    }

    @CheckResult
    @JvmOverloads
    public fun sendFile(file: File, callback: ProgressCallback? = null): Call<UploadedFile> {
        return client.sendFile(channelType, channelId, file, callback)
    }

    @CheckResult
    @JvmOverloads
    public fun sendImage(file: File, callback: ProgressCallback? = null): Call<UploadedFile> {
        return client.sendImage(channelType, channelId, file, callback)
    }

    @CheckResult
    public fun deleteFile(url: String): Call<Unit> {
        return client.deleteFile(channelType, channelId, url)
    }

    @CheckResult
    public fun deleteImage(url: String): Call<Unit> {
        return client.deleteImage(channelType, channelId, url)
    }

    @CheckResult
    public fun sendReaction(reaction: Reaction, enforceUnique: Boolean = false): Call<Reaction> {
        return client.sendReaction(reaction, enforceUnique, cid)
    }

    @CheckResult
    public fun sendAction(request: SendActionRequest): Call<Message> {
        return client.sendAction(request)
    }

    @CheckResult
    public fun deleteReaction(messageId: String, reactionType: String): Call<Message> {
        return client.deleteReaction(messageId = messageId, reactionType = reactionType, cid = cid)
    }

    @CheckResult
    public fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>> {
        return client.getReactions(messageId, offset, limit)
    }

    @CheckResult
    public fun getReactions(
        messageId: String,
        firstReactionId: String,
        limit: Int,
    ): Call<List<Message>> {
        return client.getRepliesMore(messageId, firstReactionId, limit)
    }

    @CheckResult
    public fun update(message: Message? = null, extraData: Map<String, Any> = emptyMap()): Call<Channel> {
        return client.updateChannel(channelType, channelId, message, extraData)
    }

    @CheckResult
    public fun updatePartial(set: Map<String, Any> = emptyMap(), unset: List<String> = emptyList()): Call<Channel> {
        return client.updateChannelPartial(channelType, channelId, set, unset)
    }

    @CheckResult
    public fun enableSlowMode(cooldownTimeInSeconds: Int): Call<Channel> =
        client.enableSlowMode(channelType, channelId, cooldownTimeInSeconds)

    @CheckResult
    public fun disableSlowMode(): Call<Channel> =
        client.disableSlowMode(channelType, channelId)

    @CheckResult
    public fun addMembers(
        memberIds: List<String>,
        systemMessage: Message? = null,
        hideHistory: Boolean? = null,
        skipPush: Boolean? = null,
    ): Call<Channel> {
        return client.addMembers(
            channelType = channelType,
            channelId = channelId,
            memberIds = memberIds,
            systemMessage = systemMessage,
            hideHistory = hideHistory,
            skipPush = skipPush,
        )
    }

    @CheckResult
    public fun removeMembers(
        memberIds: List<String>,
        systemMessage: Message? = null,
        skipPush: Boolean? = null,
    ): Call<Channel> {
        return client.removeMembers(
            channelType = channelType,
            channelId = channelId,
            memberIds = memberIds,
            systemMessage = systemMessage,
            skipPush = skipPush,
        )
    }

    @CheckResult
    public fun inviteMembers(
        memberIds: List<String>,
        systemMessage: Message? = null,
        skipPush: Boolean? = null,
    ): Call<Channel> {
        return client.inviteMembers(
            channelType = channelType,
            channelId = channelId,
            memberIds = memberIds,
            systemMessage = systemMessage,
            skipPush = skipPush,
        )
    }

    @CheckResult
    public fun acceptInvite(message: String?): Call<Channel> {
        return client.acceptInvite(channelType, channelId, message)
    }

    @CheckResult
    public fun rejectInvite(): Call<Channel> {
        return client.rejectInvite(channelType, channelId)
    }

    @JvmOverloads
    @CheckResult
    public fun mute(expiration: Int? = null): Call<Unit> {
        return client.muteChannel(channelType, channelId, expiration)
    }

    @CheckResult
    public fun unmute(): Call<Unit> {
        return client.unmuteChannel(channelType, channelId)
    }

    @JvmOverloads
    @CheckResult
    public fun muteUser(userId: String, timeout: Int? = null): Call<Mute> {
        return client.muteUser(userId, timeout)
    }

    @CheckResult
    public fun unmuteUser(userId: String): Call<Unit> {
        return client.unmuteUser(userId)
    }

    @CheckResult
    public fun muteCurrentUser(): Call<Mute> {
        return client.muteCurrentUser()
    }

    @CheckResult
    public fun unmuteCurrentUser(): Call<Unit> {
        return client.unmuteCurrentUser()
    }

    @CheckResult
    @JvmOverloads
    public fun keystroke(parentId: String? = null): Call<ChatEvent> {
        return client.keystroke(channelType, channelId, parentId)
    }

    @CheckResult
    @JvmOverloads
    public fun stopTyping(parentId: String? = null): Call<ChatEvent> {
        return client.stopTyping(channelType, channelId, parentId)
    }

    @CheckResult
    public fun sendEvent(
        eventType: String,
        extraData: Map<Any, Any> = emptyMap(),
    ): Call<ChatEvent> {
        return client.sendEvent(eventType, channelType, channelId, extraData)
    }

    @CheckResult
    public fun queryMembers(
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member> = emptyList(),
    ): Call<List<Member>> {
        return client.queryMembers(channelType, channelId, offset, limit, filter, sort, members)
    }

    @CheckResult
    public fun getFileAttachments(offset: Int, limit: Int): Call<List<Attachment>> =
        client.getFileAttachments(channelType, channelId, offset, limit)

    @CheckResult
    public fun getImageAttachments(offset: Int, limit: Int): Call<List<Attachment>> =
        client.getImageAttachments(channelType, channelId, offset, limit)

    @CheckResult
    public fun getMessagesWithAttachments(offset: Int, limit: Int, types: List<String>): Call<List<Message>> {
        return client.getMessagesWithAttachments(
            channelType = channelType,
            channelId = channelId,
            offset = offset,
            limit = limit,
            types = types,
        )
    }

    @CheckResult
    public fun getPinnedMessages(
        limit: Int,
        sort: QuerySorter<Message>,
        pagination: PinnedMessagesPagination,
    ): Call<List<Message>> {
        return client.getPinnedMessages(
            channelType = channelType,
            channelId = channelId,
            limit = limit,
            sort = sort,
            pagination = pagination,
        )
    }

    @CheckResult
    public fun pinMessage(message: Message, expirationDate: Date?): Call<Message> {
        return client.pinMessage(message, expirationDate)
    }

    @CheckResult
    public fun pinMessage(message: Message, timeout: Int): Call<Message> {
        return client.pinMessage(message, timeout)
    }

    @CheckResult
    public fun unpinMessage(message: Message): Call<Message> = client.unpinMessage(message)
}
package com.example.composechatsample.core.events

import com.example.composechatsample.core.DisconnectCause
import com.example.composechatsample.core.errors.ChatError
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.User
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger
import com.example.composechatsample.core.Error
private val seqGenerator = AtomicInteger()


public sealed class ChatEvent {
    public abstract val type: String
    public abstract val createdAt: Date
    public abstract val rawCreatedAt: String?

    public val seq: Int = seqGenerator.incrementAndGet()
}

public sealed class CidEvent : ChatEvent() {
    public abstract val cid: String
    public abstract val channelType: String
    public abstract val channelId: String
}

public sealed interface UserEvent {
    public val user: User
}

public sealed interface HasChannel {
    public val channel: Channel
}

public sealed interface HasMessage {
    public val message: Message
}

public sealed interface HasReaction {
    public val reaction: Reaction
}

public sealed interface HasMember {
    public val member: Member
}

public sealed interface HasOwnUser {
    public val me: User
}

public sealed interface HasWatcherCount {
    public val watcherCount: Int
}

public sealed interface HasUnreadCounts {
    public val totalUnreadCount: Int
    public val unreadChannels: Int
}

public data class ChannelDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    val user: User?,
) : CidEvent(), HasChannel

public data class ChannelHiddenEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    val clearHistory: Boolean,
) : CidEvent(), UserEvent

public data class ChannelTruncatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    val user: User?,
    val message: Message?,
    override val channel: Channel,
) : CidEvent(), HasChannel

public data class ChannelUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    val message: Message?,
    override val channel: Channel,
) : CidEvent(), HasChannel

public data class ChannelUpdatedByUserEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    val message: Message?,
    override val channel: Channel,
) : CidEvent(), UserEvent, HasChannel

public data class ChannelVisibleEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
) : CidEvent(), UserEvent

public data class HealthEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    val connectionId: String,
) : ChatEvent()

public data class MemberAddedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val member: Member,
) : CidEvent(), UserEvent, HasMember

public data class MemberRemovedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val member: Member,
) : CidEvent(), UserEvent, HasMember

public data class MemberUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val member: Member,
) : CidEvent(), UserEvent, HasMember

public data class MessageDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    val user: User?,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val message: Message,
    val hardDelete: Boolean,
) : CidEvent(), HasMessage

public data class MessageReadEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
) : CidEvent(), UserEvent

public data class MessageUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val message: Message,
) : CidEvent(), UserEvent, HasMessage

public data class NewMessageEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val message: Message,
    override val watcherCount: Int = 0,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : CidEvent(), UserEvent, HasMessage, HasWatcherCount, HasUnreadCounts

public data class NotificationAddedToChannelEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    override val member: Member,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : CidEvent(), HasChannel, HasMember, HasUnreadCounts

public data class NotificationChannelDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : CidEvent(), HasChannel, HasUnreadCounts

public data class NotificationChannelMutesUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val me: User,
) : ChatEvent(), HasOwnUser

public data class NotificationChannelTruncatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : CidEvent(), HasChannel, HasUnreadCounts

public data class NotificationInviteAcceptedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    override val member: Member,
    override val channel: Channel,
) : CidEvent(), UserEvent, HasMember, HasChannel

public data class NotificationInviteRejectedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    override val member: Member,
    override val channel: Channel,
) : CidEvent(), UserEvent, HasMember, HasChannel

public data class NotificationInvitedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    override val member: Member,
) : CidEvent(), UserEvent, HasMember

public data class NotificationMarkReadEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : CidEvent(), UserEvent, HasUnreadCounts

public data class NotificationMarkUnreadEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
    val unreadMessages: Int,
    val firstUnreadMessageId: String,
    val lastReadMessageAt: Date,
    val lastReadMessageId: String,
) : CidEvent(), UserEvent, HasUnreadCounts

public data class MarkAllReadEvent(
    override val type: String = "",
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : ChatEvent(), UserEvent, HasUnreadCounts

public data class NotificationMessageNewEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    override val message: Message,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : CidEvent(), HasChannel, HasMessage, HasUnreadCounts

public data class NotificationMutesUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val me: User,
) : ChatEvent(), HasOwnUser

public data class NotificationRemovedFromChannelEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    val user: User?,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    override val member: Member,
) : CidEvent(), HasMember, HasChannel

public data class ReactionDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val message: Message,
    override val reaction: Reaction,
) : CidEvent(), UserEvent, HasMessage, HasReaction

public data class ReactionNewEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val message: Message,
    override val reaction: Reaction,
) : CidEvent(), UserEvent, HasMessage, HasReaction

public data class ReactionUpdateEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val message: Message,
    override val reaction: Reaction,
) : CidEvent(), UserEvent, HasMessage, HasReaction

public data class TypingStartEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    val parentId: String?,
) : CidEvent(), UserEvent

public data class TypingStopEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    val parentId: String?,
) : CidEvent(), UserEvent

public data class ChannelUserBannedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    val expiration: Date?,
    val shadow: Boolean,
) : CidEvent(), UserEvent

public data class GlobalUserBannedEvent(
    override val type: String,
    override val user: User,
    override val createdAt: Date,
    override val rawCreatedAt: String,
) : ChatEvent(), UserEvent

public data class UserDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
) : ChatEvent(), UserEvent

public data class UserPresenceChangedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
) : ChatEvent(), UserEvent

public data class UserStartWatchingEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val watcherCount: Int = 0,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
) : CidEvent(), UserEvent, HasWatcherCount

public data class UserStopWatchingEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val watcherCount: Int = 0,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
) : CidEvent(), UserEvent, HasWatcherCount

public data class ChannelUserUnbannedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
) : CidEvent(), UserEvent

public data class GlobalUserUnbannedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
) : ChatEvent(), UserEvent

public data class UserUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
) : ChatEvent(), UserEvent

public data class ConnectedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val me: User,
    val connectionId: String,
) : ChatEvent(), HasOwnUser

public data class ConnectionErrorEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    val connectionId: String,
    val error: ChatError,
) : ChatEvent()

public data class ConnectingEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
) : ChatEvent()

data class DisconnectedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    val disconnectCause: DisconnectCause = DisconnectCause.NetworkNotAvailable,
) : ChatEvent()

data class ErrorEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    val error: Error,
) : ChatEvent()

data class UnknownEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    val user: User?,
    val rawData: Map<*, *>,
) : ChatEvent()
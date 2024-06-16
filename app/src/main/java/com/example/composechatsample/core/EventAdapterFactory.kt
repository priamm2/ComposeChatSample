package com.example.composechatsample.core

import com.example.composechatsample.core.models.EventType
import com.example.composechatsample.core.models.dto.ChannelDeletedEventDto
import com.example.composechatsample.core.models.dto.ChannelHiddenEventDto
import com.example.composechatsample.core.models.dto.ChannelTruncatedEventDto
import com.example.composechatsample.core.models.dto.ChannelUpdatedByUserEventDto
import com.example.composechatsample.core.models.dto.ChannelUpdatedEventDto
import com.example.composechatsample.core.models.dto.ChannelUserBannedEventDto
import com.example.composechatsample.core.models.dto.ChannelUserUnbannedEventDto
import com.example.composechatsample.core.models.dto.ChannelVisibleEventDto
import com.example.composechatsample.core.models.dto.ChatEventDto
import com.example.composechatsample.core.models.dto.ConnectedEventDto
import com.example.composechatsample.core.models.dto.ConnectionErrorEventDto
import com.example.composechatsample.core.models.dto.DownstreamUserDto
import com.example.composechatsample.core.models.dto.ExactDate
import com.example.composechatsample.core.models.dto.GlobalUserBannedEventDto
import com.example.composechatsample.core.models.dto.GlobalUserUnbannedEventDto
import com.example.composechatsample.core.models.dto.HealthEventDto
import com.example.composechatsample.core.models.dto.MarkAllReadEventDto
import com.example.composechatsample.core.models.dto.MemberAddedEventDto
import com.example.composechatsample.core.models.dto.MemberRemovedEventDto
import com.example.composechatsample.core.models.dto.MemberUpdatedEventDto
import com.example.composechatsample.core.models.dto.MessageDeletedEventDto
import com.example.composechatsample.core.models.dto.MessageReadEventDto
import com.example.composechatsample.core.models.dto.MessageUpdatedEventDto
import com.example.composechatsample.core.models.dto.NewMessageEventDto
import com.example.composechatsample.core.models.dto.NotificationAddedToChannelEventDto
import com.example.composechatsample.core.models.dto.NotificationChannelDeletedEventDto
import com.example.composechatsample.core.models.dto.NotificationChannelMutesUpdatedEventDto
import com.example.composechatsample.core.models.dto.NotificationChannelTruncatedEventDto
import com.example.composechatsample.core.models.dto.NotificationInviteAcceptedEventDto
import com.example.composechatsample.core.models.dto.NotificationInviteRejectedEventDto
import com.example.composechatsample.core.models.dto.NotificationInvitedEventDto
import com.example.composechatsample.core.models.dto.NotificationMarkReadEventDto
import com.example.composechatsample.core.models.dto.NotificationMarkUnreadEventDto
import com.example.composechatsample.core.models.dto.NotificationMessageNewEventDto
import com.example.composechatsample.core.models.dto.NotificationMutesUpdatedEventDto
import com.example.composechatsample.core.models.dto.NotificationRemovedFromChannelEventDto
import com.example.composechatsample.core.models.dto.ReactionDeletedEventDto
import com.example.composechatsample.core.models.dto.ReactionNewEventDto
import com.example.composechatsample.core.models.dto.ReactionUpdateEventDto
import com.example.composechatsample.core.models.dto.TypingStartEventDto
import com.example.composechatsample.core.models.dto.TypingStopEventDto
import com.example.composechatsample.core.models.dto.UnknownEventDto
import com.example.composechatsample.core.models.dto.UserDeletedEventDto
import com.example.composechatsample.core.models.dto.UserPresenceChangedEventDto
import com.example.composechatsample.core.models.dto.UserStartWatchingEventDto
import com.example.composechatsample.core.models.dto.UserStopWatchingEventDto
import com.example.composechatsample.core.models.dto.UserUpdatedEventDto
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.rawType
import java.lang.reflect.Type

class EventAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return when (type.rawType) {
            ChatEventDto::class.java -> EventDtoAdapter(moshi)
            else -> null
        }
    }
}

internal class EventDtoAdapter(
    private val moshi: Moshi,
) : JsonAdapter<ChatEventDto>() {

    private val mapAdapter: JsonAdapter<MutableMap<String, Any?>> =
        moshi.adapter(Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java))

    private val connectedEventAdapter = moshi.adapter(ConnectedEventDto::class.java)
    private val connectionErrorEventAdapter = moshi.adapter(ConnectionErrorEventDto::class.java)
    private val healthEventAdapter = moshi.adapter(HealthEventDto::class.java)
    private val newMessageEventAdapter = moshi.adapter(NewMessageEventDto::class.java)
    private val messageDeletedEventAdapter = moshi.adapter(MessageDeletedEventDto::class.java)
    private val messageUpdatedEventAdapter = moshi.adapter(MessageUpdatedEventDto::class.java)
    private val messageReadEventAdapter = moshi.adapter(MessageReadEventDto::class.java)
    private val typingStartEventAdapter = moshi.adapter(TypingStartEventDto::class.java)
    private val typingStopEventAdapter = moshi.adapter(TypingStopEventDto::class.java)
    private val reactionNewEventAdapter = moshi.adapter(ReactionNewEventDto::class.java)
    private val reactionUpdateEventAdapter = moshi.adapter(ReactionUpdateEventDto::class.java)
    private val reactionDeletedEventAdapter = moshi.adapter(ReactionDeletedEventDto::class.java)
    private val memberAddedEventAdapter = moshi.adapter(MemberAddedEventDto::class.java)
    private val memberRemovedEventAdapter = moshi.adapter(MemberRemovedEventDto::class.java)
    private val memberUpdatedEventAdapter = moshi.adapter(MemberUpdatedEventDto::class.java)
    private val channelUpdatedByUserEventAdapter = moshi.adapter(ChannelUpdatedByUserEventDto::class.java)
    private val channelUpdatedEventAdapter = moshi.adapter(ChannelUpdatedEventDto::class.java)
    private val channelHiddenEventAdapter = moshi.adapter(ChannelHiddenEventDto::class.java)
    private val channelDeletedEventAdapter = moshi.adapter(ChannelDeletedEventDto::class.java)
    private val channelVisibleEventAdapter = moshi.adapter(ChannelVisibleEventDto::class.java)
    private val channelTruncatedEventAdapter = moshi.adapter(ChannelTruncatedEventDto::class.java)
    private val userStartWatchingEventAdapter = moshi.adapter(UserStartWatchingEventDto::class.java)
    private val userStopWatchingEventAdapter = moshi.adapter(UserStopWatchingEventDto::class.java)
    private val notificationAddedToChannelEventAdapter = moshi.adapter(
        NotificationAddedToChannelEventDto::class.java)
    private val notificationMarkReadEventAdapter = moshi.adapter(NotificationMarkReadEventDto::class.java)
    private val notificationMarkUnreadEventAdapter = moshi.adapter(NotificationMarkUnreadEventDto::class.java)
    private val markAllReadEventAdapter = moshi.adapter(MarkAllReadEventDto::class.java)
    private val notificationMessageNewEventAdapter = moshi.adapter(NotificationMessageNewEventDto::class.java)
    private val notificationInvitedEventAdapter = moshi.adapter(NotificationInvitedEventDto::class.java)
    private val notificationInviteAcceptedEventAdapter = moshi.adapter(
        NotificationInviteAcceptedEventDto::class.java)
    private val notificationInviteRejectedEventAdapter = moshi.adapter(
        NotificationInviteRejectedEventDto::class.java)
    private val notificationRemovedFromChannelEventAdapter =
        moshi.adapter(NotificationRemovedFromChannelEventDto::class.java)
    private val notificationMutesUpdatedEventAdapter = moshi.adapter(
        NotificationMutesUpdatedEventDto::class.java)
    private val notificationChannelMutesUpdatedEventAdapter =
        moshi.adapter(NotificationChannelMutesUpdatedEventDto::class.java)
    private val notificationChannelDeletedEventAdapter = moshi.adapter(
        NotificationChannelDeletedEventDto::class.java)
    private val notificationChannelTruncatedEventAdapter =
        moshi.adapter(NotificationChannelTruncatedEventDto::class.java)
    private val userPresenceChangedEventAdapter = moshi.adapter(UserPresenceChangedEventDto::class.java)
    private val userUpdatedEventAdapter = moshi.adapter(UserUpdatedEventDto::class.java)
    private val userDeletedEventAdapter = moshi.adapter(UserDeletedEventDto::class.java)
    private val channelUserBannedEventAdapter = moshi.adapter(ChannelUserBannedEventDto::class.java)
    private val globalUserBannedEventAdapter = moshi.adapter(GlobalUserBannedEventDto::class.java)
    private val channelUserUnbannedEventAdapter = moshi.adapter(ChannelUserUnbannedEventDto::class.java)
    private val globalUserUnbannedEventAdapter = moshi.adapter(GlobalUserUnbannedEventDto::class.java)

    @Suppress("LongMethod", "ComplexMethod", "ReturnCount")
    override fun fromJson(reader: JsonReader): ChatEventDto? {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull<Nothing?>()
            return null
        }

        val map: Map<String, Any?> = mapAdapter.fromJson(reader)!!.filterValues { it != null }

        val adapter = when (val type = map["type"] as? String) {
            EventType.HEALTH_CHECK -> when {
                map.containsKey("me") -> connectedEventAdapter
                else -> healthEventAdapter
            }
            EventType.CONNECTION_ERROR -> connectionErrorEventAdapter
            EventType.MESSAGE_NEW -> newMessageEventAdapter
            EventType.MESSAGE_DELETED -> messageDeletedEventAdapter
            EventType.MESSAGE_UPDATED -> messageUpdatedEventAdapter
            EventType.MESSAGE_READ -> when {
                map.containsKey("cid") -> messageReadEventAdapter
                else -> markAllReadEventAdapter
            }
            EventType.TYPING_START -> typingStartEventAdapter
            EventType.TYPING_STOP -> typingStopEventAdapter
            EventType.REACTION_NEW -> reactionNewEventAdapter
            EventType.REACTION_UPDATED -> reactionUpdateEventAdapter
            EventType.REACTION_DELETED -> reactionDeletedEventAdapter
            EventType.MEMBER_ADDED -> memberAddedEventAdapter
            EventType.MEMBER_REMOVED -> memberRemovedEventAdapter
            EventType.MEMBER_UPDATED -> memberUpdatedEventAdapter
            EventType.CHANNEL_UPDATED -> when {
                map.containsKey("user") -> channelUpdatedByUserEventAdapter
                else -> channelUpdatedEventAdapter
            }
            EventType.CHANNEL_HIDDEN -> channelHiddenEventAdapter
            EventType.CHANNEL_DELETED -> channelDeletedEventAdapter
            EventType.CHANNEL_VISIBLE -> channelVisibleEventAdapter
            EventType.CHANNEL_TRUNCATED -> channelTruncatedEventAdapter
            EventType.USER_WATCHING_START -> userStartWatchingEventAdapter
            EventType.USER_WATCHING_STOP -> userStopWatchingEventAdapter
            EventType.NOTIFICATION_ADDED_TO_CHANNEL -> notificationAddedToChannelEventAdapter
            EventType.NOTIFICATION_MARK_READ -> when {
                map.containsKey("cid") -> notificationMarkReadEventAdapter
                else -> markAllReadEventAdapter
            }
            EventType.NOTIFICATION_MARK_UNREAD -> notificationMarkUnreadEventAdapter
            EventType.NOTIFICATION_MESSAGE_NEW -> notificationMessageNewEventAdapter
            EventType.NOTIFICATION_INVITED -> notificationInvitedEventAdapter
            EventType.NOTIFICATION_INVITE_ACCEPTED -> notificationInviteAcceptedEventAdapter
            EventType.NOTIFICATION_INVITE_REJECTED -> notificationInviteRejectedEventAdapter
            EventType.NOTIFICATION_REMOVED_FROM_CHANNEL -> notificationRemovedFromChannelEventAdapter
            EventType.NOTIFICATION_MUTES_UPDATED -> notificationMutesUpdatedEventAdapter
            EventType.NOTIFICATION_CHANNEL_MUTES_UPDATED -> notificationChannelMutesUpdatedEventAdapter
            EventType.NOTIFICATION_CHANNEL_DELETED -> notificationChannelDeletedEventAdapter
            EventType.NOTIFICATION_CHANNEL_TRUNCATED -> notificationChannelTruncatedEventAdapter
            EventType.USER_PRESENCE_CHANGED -> userPresenceChangedEventAdapter
            EventType.USER_UPDATED -> userUpdatedEventAdapter
            EventType.USER_DELETED -> userDeletedEventAdapter
            EventType.USER_BANNED -> when {
                map.containsKey("cid") -> channelUserBannedEventAdapter
                else -> globalUserBannedEventAdapter
            }
            EventType.USER_UNBANNED -> when {
                map.containsKey("cid") -> channelUserUnbannedEventAdapter
                else -> globalUserUnbannedEventAdapter
            }
            else -> // Custom case, early return
                return UnknownEventDto(
                    type = type ?: EventType.UNKNOWN,
                    created_at = moshi.adapter(ExactDate::class.java).fromJsonValue(map["created_at"])!!,
                    user = moshi.adapter(DownstreamUserDto::class.java).fromJsonValue(map["user"]),
                    rawData = map,
                )
        }

        return adapter.fromJsonValue(map)
    }

    override fun toJson(writer: JsonWriter, value: ChatEventDto?) {
        error("Can't convert this event to Json $value")
    }
}
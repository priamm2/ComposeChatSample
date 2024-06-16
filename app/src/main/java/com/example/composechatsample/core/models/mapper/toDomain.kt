package com.example.composechatsample.core.models.mapper

import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.errors.ChatError
import com.example.composechatsample.core.errors.ChatErrorDetail
import com.example.composechatsample.core.errors.ErrorDetail
import com.example.composechatsample.core.errors.ErrorResponse
import com.example.composechatsample.core.errors.SocketErrorMessage
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
import com.example.composechatsample.core.models.AgoraChannel
import com.example.composechatsample.core.models.App
import com.example.composechatsample.core.models.AppSettings
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.BannedUser
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.ChannelInfo
import com.example.composechatsample.core.models.ChannelMute
import com.example.composechatsample.core.models.ChannelUserRead
import com.example.composechatsample.core.models.Command
import com.example.composechatsample.core.models.Config
import com.example.composechatsample.core.models.Device
import com.example.composechatsample.core.models.FileUploadConfig
import com.example.composechatsample.core.models.Flag
import com.example.composechatsample.core.models.HMSRoom
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.MessageModerationAction
import com.example.composechatsample.core.models.MessageModerationDetails
import com.example.composechatsample.core.models.Mute
import com.example.composechatsample.core.models.PushProvider
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.ReactionGroup
import com.example.composechatsample.core.models.SearchWarning
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.VideoCallInfo
import com.example.composechatsample.core.models.VideoCallToken
import com.example.composechatsample.core.models.dto.AgoraDto
import com.example.composechatsample.core.models.dto.AttachmentDto
import com.example.composechatsample.core.models.dto.ChannelDeletedEventDto
import com.example.composechatsample.core.models.dto.ChannelHiddenEventDto
import com.example.composechatsample.core.models.dto.ChannelInfoDto
import com.example.composechatsample.core.models.dto.ChannelTruncatedEventDto
import com.example.composechatsample.core.models.dto.ChannelUpdatedByUserEventDto
import com.example.composechatsample.core.models.dto.ChannelUpdatedEventDto
import com.example.composechatsample.core.models.dto.ChannelUserBannedEventDto
import com.example.composechatsample.core.models.dto.ChannelUserUnbannedEventDto
import com.example.composechatsample.core.models.dto.ChannelVisibleEventDto
import com.example.composechatsample.core.models.dto.ChatEventDto
import com.example.composechatsample.core.models.dto.CommandDto
import com.example.composechatsample.core.models.dto.ConfigDto
import com.example.composechatsample.core.models.dto.ConnectedEventDto
import com.example.composechatsample.core.models.dto.ConnectingEventDto
import com.example.composechatsample.core.models.dto.ConnectionErrorEventDto
import com.example.composechatsample.core.models.dto.DeviceDto
import com.example.composechatsample.core.models.dto.DisconnectedEventDto
import com.example.composechatsample.core.models.dto.DownstreamChannelDto
import com.example.composechatsample.core.models.dto.DownstreamChannelMuteDto
import com.example.composechatsample.core.models.dto.DownstreamChannelUserRead
import com.example.composechatsample.core.models.dto.DownstreamFlagDto
import com.example.composechatsample.core.models.dto.DownstreamMemberDto
import com.example.composechatsample.core.models.dto.DownstreamMessageDto
import com.example.composechatsample.core.models.dto.DownstreamModerationDetailsDto
import com.example.composechatsample.core.models.dto.DownstreamMuteDto
import com.example.composechatsample.core.models.dto.DownstreamReactionDto
import com.example.composechatsample.core.models.dto.DownstreamReactionGroupDto
import com.example.composechatsample.core.models.dto.DownstreamUserDto
import com.example.composechatsample.core.models.dto.ErrorDetailDto
import com.example.composechatsample.core.models.dto.ErrorDto
import com.example.composechatsample.core.models.dto.ErrorEventDto
import com.example.composechatsample.core.models.dto.GlobalUserBannedEventDto
import com.example.composechatsample.core.models.dto.GlobalUserUnbannedEventDto
import com.example.composechatsample.core.models.dto.HMSDto
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
import com.example.composechatsample.core.models.dto.SearchWarningDto
import com.example.composechatsample.core.models.dto.SocketErrorResponse
import com.example.composechatsample.core.models.dto.TypingStartEventDto
import com.example.composechatsample.core.models.dto.TypingStopEventDto
import com.example.composechatsample.core.models.dto.UnknownEventDto
import com.example.composechatsample.core.models.dto.UpstreamMemberDto
import com.example.composechatsample.core.models.dto.UpstreamUserDto
import com.example.composechatsample.core.models.dto.UserDeletedEventDto
import com.example.composechatsample.core.models.dto.UserPresenceChangedEventDto
import com.example.composechatsample.core.models.dto.UserStartWatchingEventDto
import com.example.composechatsample.core.models.dto.UserStopWatchingEventDto
import com.example.composechatsample.core.models.dto.UserUpdatedEventDto
import com.example.composechatsample.core.models.response.AppDto
import com.example.composechatsample.core.models.response.AppSettingsResponse
import com.example.composechatsample.core.models.response.BannedUserResponse
import com.example.composechatsample.core.models.response.CreateVideoCallResponse
import com.example.composechatsample.core.models.response.FileUploadConfigDto
import com.example.composechatsample.core.models.response.VideoCallTokenResponse
import java.util.Date

fun AppSettingsResponse.toDomain(): AppSettings {
    return AppSettings(
        app = app.toDomain(),
    )
}

fun AppDto.toDomain(): App {
    return App(
        name = name,
        fileUploadConfig = file_upload_config.toDomain(),
        imageUploadConfig = image_upload_config.toDomain(),
    )
}

fun FileUploadConfigDto.toDomain(): FileUploadConfig {
    return FileUploadConfig(
        allowedFileExtensions = allowed_file_extensions,
        allowedMimeTypes = allowed_mime_types,
        blockedFileExtensions = blocked_file_extensions,
        blockedMimeTypes = blocked_mime_types,
        sizeLimitInBytes = size_limit?.takeUnless { it <= 0 } ?: AppSettings.DEFAULT_SIZE_LIMIT_IN_BYTES,
    )
}

fun AttachmentDto.toDomain(): Attachment =
    Attachment(
        assetUrl = asset_url,
        authorName = author_name,
        authorLink = author_link,
        fallback = fallback,
        fileSize = file_size,
        image = image,
        imageUrl = image_url,
        mimeType = mime_type,
        name = name,
        ogUrl = og_scrape_url,
        text = text,
        thumbUrl = thumb_url,
        title = title,
        titleLink = title_link,
        type = type,
        url = url,
        originalHeight = original_height,
        originalWidth = original_width,
        extraData = extraData.toMutableMap(),
    )

fun BannedUserResponse.toDomain(): BannedUser {
    return BannedUser(
        user = user.toDomain(),
        bannedBy = banned_by?.toDomain(),
        channel = channel?.toDomain(),
        createdAt = created_at,
        expires = expires,
        shadow = shadow,
        reason = reason,
    )
}

fun ChannelInfoDto.toDomain(): ChannelInfo =
    ChannelInfo(
        cid = cid,
        id = id,
        memberCount = member_count,
        name = name,
        type = type,
        image = image,
    )

fun DownstreamChannelDto.toDomain(): Channel =
    Channel(
        id = id,
        type = type,
        name = name ?: "",
        image = image ?: "",
        watcherCount = watcher_count,
        frozen = frozen,
        lastMessageAt = last_message_at,
        createdAt = created_at,
        deletedAt = deleted_at,
        updatedAt = updated_at,
        memberCount = member_count,
        messages = messages.map(DownstreamMessageDto::toDomain),
        members = members.map(DownstreamMemberDto::toDomain),
        watchers = watchers.map(DownstreamUserDto::toDomain),
        read = read.map { it.toDomain(last_message_at ?: it.last_read) },
        config = config.toDomain(),
        createdBy = created_by?.toDomain() ?: User(),
        team = team,
        cooldown = cooldown,
        pinnedMessages = pinned_messages.map(DownstreamMessageDto::toDomain),
        ownCapabilities = own_capabilities.toSet(),
        membership = membership?.toDomain(),
        extraData = extraData.toMutableMap(),
    ).syncUnreadCountWithReads()

public fun Channel.syncUnreadCountWithReads(): Channel =
    copy(unreadCount = currentUserUnreadCount)

public val Channel.currentUserUnreadCount: Int
    get() = ChatClient.instance().getCurrentUser()?.let { currentUser ->
        read.firstOrNull { it.user.id == currentUser.id }?.unreadMessages
    } ?: 0



fun DownstreamChannelMuteDto.toDomain(): ChannelMute =
    ChannelMute(
        user = user.toDomain(),
        channel = channel.toDomain(),
        createdAt = created_at,
        updatedAt = updated_at,
        expires = expires,
    )

fun DownstreamChannelUserRead.toDomain(lastReceivedEventDate: Date): ChannelUserRead =
    ChannelUserRead(
        user = user.toDomain(),
        lastReceivedEventDate = lastReceivedEventDate,
        lastRead = last_read,
        unreadMessages = unread_messages,
        lastReadMessageId = last_read_message_id,
    )

fun CommandDto.toDomain(): Command {
    return Command(
        name = name,
        description = description,
        args = args,
        set = set,
    )
}

fun ConfigDto.toDomain(): Config =
    Config(
        createdAt = created_at,
        updatedAt = updated_at,
        name = name ?: "",
        typingEventsEnabled = typing_events,
        readEventsEnabled = read_events,
        connectEventsEnabled = connect_events,
        searchEnabled = search,
        isReactionsEnabled = reactions,
        isThreadEnabled = replies,
        muteEnabled = mutes,
        uploadsEnabled = uploads,
        urlEnrichmentEnabled = url_enrichment,
        customEventsEnabled = custom_events,
        pushNotificationsEnabled = push_notifications,
        messageRetention = message_retention,
        maxMessageLength = max_message_length,
        automod = automod,
        automodBehavior = automod_behavior,
        blocklistBehavior = blocklist_behavior ?: "",
        commands = commands.map(CommandDto::toDomain),
    )

fun DeviceDto.toDomain(): Device =
    Device(
        token = id,
        pushProvider = PushProvider.fromKey(push_provider),
        providerName = provider_name,
    )

fun ErrorDto.toDomain(): ChatError {
    val dto = this
    return ChatError(
        code = dto.code,
        message = dto.message,
        statusCode = dto.StatusCode,
        exceptionFields = dto.exception_fields,
        moreInfo = dto.more_info,
        details = dto.details.map { it.toDomain() },
    ).apply {
        duration = dto.duration
    }
}

internal fun ErrorDetailDto.toDomain(): ChatErrorDetail {
    val dto = this
    return ChatErrorDetail(
        code = dto.code,
        messages = dto.messages,
    )
}

internal fun ChatEventDto.toDomain(): ChatEvent {
    return when (this) {
        is NewMessageEventDto -> toDomain()
        is ChannelDeletedEventDto -> toDomain()
        is ChannelHiddenEventDto -> toDomain()
        is ChannelTruncatedEventDto -> toDomain()
        is ChannelUpdatedByUserEventDto -> toDomain()
        is ChannelUpdatedEventDto -> toDomain()
        is ChannelUserBannedEventDto -> toDomain()
        is ChannelUserUnbannedEventDto -> toDomain()
        is ChannelVisibleEventDto -> toDomain()
        is ConnectedEventDto -> toDomain()
        is ConnectionErrorEventDto -> toDomain()
        is ConnectingEventDto -> toDomain()
        is DisconnectedEventDto -> toDomain()
        is ErrorEventDto -> toDomain()
        is GlobalUserBannedEventDto -> toDomain()
        is GlobalUserUnbannedEventDto -> toDomain()
        is HealthEventDto -> toDomain()
        is MarkAllReadEventDto -> toDomain()
        is MemberAddedEventDto -> toDomain()
        is MemberRemovedEventDto -> toDomain()
        is MemberUpdatedEventDto -> toDomain()
        is MessageDeletedEventDto -> toDomain()
        is MessageReadEventDto -> toDomain()
        is MessageUpdatedEventDto -> toDomain()
        is NotificationAddedToChannelEventDto -> toDomain()
        is NotificationChannelDeletedEventDto -> toDomain()
        is NotificationChannelMutesUpdatedEventDto -> toDomain()
        is NotificationChannelTruncatedEventDto -> toDomain()
        is NotificationInviteAcceptedEventDto -> toDomain()
        is NotificationInviteRejectedEventDto -> toDomain()
        is NotificationInvitedEventDto -> toDomain()
        is NotificationMarkReadEventDto -> toDomain()
        is NotificationMarkUnreadEventDto -> toDomain()
        is NotificationMessageNewEventDto -> toDomain()
        is NotificationMutesUpdatedEventDto -> toDomain()
        is NotificationRemovedFromChannelEventDto -> toDomain()
        is ReactionDeletedEventDto -> toDomain()
        is ReactionNewEventDto -> toDomain()
        is ReactionUpdateEventDto -> toDomain()
        is TypingStartEventDto -> toDomain()
        is TypingStopEventDto -> toDomain()
        is UnknownEventDto -> toDomain()
        is UserDeletedEventDto -> toDomain()
        is UserPresenceChangedEventDto -> toDomain()
        is UserStartWatchingEventDto -> toDomain()
        is UserStopWatchingEventDto -> toDomain()
        is UserUpdatedEventDto -> toDomain()
    }
}

private fun ChannelDeletedEventDto.toDomain(): ChannelDeletedEvent {
    return ChannelDeletedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(),
        user = user?.toDomain(),
    )
}

private fun ChannelHiddenEventDto.toDomain(): ChannelHiddenEvent {
    return ChannelHiddenEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
        clearHistory = clear_history,
    )
}

private fun ChannelTruncatedEventDto.toDomain(): ChannelTruncatedEvent {
    return ChannelTruncatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user?.toDomain(),
        message = message?.toDomain(),
        channel = channel.toDomain(),
    )
}

private fun ChannelUpdatedEventDto.toDomain(): ChannelUpdatedEvent {
    return ChannelUpdatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message?.toDomain(),
        channel = channel.toDomain(),
    )
}

private fun ChannelUpdatedByUserEventDto.toDomain(): ChannelUpdatedByUserEvent {
    return ChannelUpdatedByUserEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
        message = message?.toDomain(),
        channel = channel.toDomain(),
    )
}

private fun ChannelVisibleEventDto.toDomain(): ChannelVisibleEvent {
    return ChannelVisibleEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
    )
}

private fun HealthEventDto.toDomain(): HealthEvent {
    return HealthEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        connectionId = connection_id,
    )
}

private fun MemberAddedEventDto.toDomain(): MemberAddedEvent {
    return MemberAddedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        member = member.toDomain(),
    )
}

private fun MemberRemovedEventDto.toDomain(): MemberRemovedEvent {
    return MemberRemovedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        member = member.toDomain(),
    )
}

private fun MemberUpdatedEventDto.toDomain(): MemberUpdatedEvent {
    return MemberUpdatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        member = member.toDomain(),
    )
}

private fun MessageDeletedEventDto.toDomain(): MessageDeletedEvent {
    // TODO review createdAt and deletedAt fields here
    return MessageDeletedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user?.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(),
        hardDelete = hard_delete ?: false,
    )
}

private fun MessageReadEventDto.toDomain(): MessageReadEvent {
    return MessageReadEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
    )
}

private fun MessageUpdatedEventDto.toDomain(): MessageUpdatedEvent {
    return MessageUpdatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(),
    )
}

private fun NewMessageEventDto.toDomain(): NewMessageEvent {
    return NewMessageEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(),
        watcherCount = watcher_count,
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
    )
}

private fun NotificationAddedToChannelEventDto.toDomain(): NotificationAddedToChannelEvent {
    return NotificationAddedToChannelEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(),
        member = member.toDomain(),
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
    )
}

private fun NotificationChannelDeletedEventDto.toDomain(): NotificationChannelDeletedEvent {
    return NotificationChannelDeletedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(),
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
    )
}

private fun NotificationChannelMutesUpdatedEventDto.toDomain(): NotificationChannelMutesUpdatedEvent {
    return NotificationChannelMutesUpdatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        me = me.toDomain(),
    )
}

private fun NotificationChannelTruncatedEventDto.toDomain(): NotificationChannelTruncatedEvent {
    return NotificationChannelTruncatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(),
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
    )
}

private fun NotificationInviteAcceptedEventDto.toDomain(): NotificationInviteAcceptedEvent {
    return NotificationInviteAcceptedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
        member = member.toDomain(),
        channel = channel.toDomain(),
    )
}

private fun NotificationInviteRejectedEventDto.toDomain(): NotificationInviteRejectedEvent {
    return NotificationInviteRejectedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
        member = member.toDomain(),
        channel = channel.toDomain(),
    )
}

private fun NotificationInvitedEventDto.toDomain(): NotificationInvitedEvent {
    return NotificationInvitedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
        member = member.toDomain(),
    )
}

private fun NotificationMarkReadEventDto.toDomain(): NotificationMarkReadEvent {
    return NotificationMarkReadEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
    )
}

private fun NotificationMarkUnreadEventDto.toDomain(): NotificationMarkUnreadEvent {
    return NotificationMarkUnreadEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
        firstUnreadMessageId = first_unread_message_id,
        lastReadMessageId = last_read_message_id,
        lastReadMessageAt = last_read_at.date,
        unreadMessages = unread_messages,
    )
}

private fun MarkAllReadEventDto.toDomain(): MarkAllReadEvent {
    return MarkAllReadEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
    )
}

private fun NotificationMessageNewEventDto.toDomain(): NotificationMessageNewEvent {
    return NotificationMessageNewEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(),
        message = message.toDomain(),
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
    )
}

private fun NotificationMutesUpdatedEventDto.toDomain(): NotificationMutesUpdatedEvent {
    return NotificationMutesUpdatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        me = me.toDomain(),
    )
}

private fun NotificationRemovedFromChannelEventDto.toDomain(): NotificationRemovedFromChannelEvent {
    return NotificationRemovedFromChannelEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user?.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(),
        member = member.toDomain(),
    )
}

private fun ReactionDeletedEventDto.toDomain(): ReactionDeletedEvent {
    return ReactionDeletedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(),
        reaction = reaction.toDomain(),
    )
}

private fun ReactionNewEventDto.toDomain(): ReactionNewEvent {
    return ReactionNewEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(),
        reaction = reaction.toDomain(),
    )
}

private fun ReactionUpdateEventDto.toDomain(): ReactionUpdateEvent {
    return ReactionUpdateEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(),
        reaction = reaction.toDomain(),
    )
}

private fun TypingStartEventDto.toDomain(): TypingStartEvent {
    return TypingStartEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        parentId = parent_id,
    )
}

private fun TypingStopEventDto.toDomain(): TypingStopEvent {
    return TypingStopEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        parentId = parent_id,
    )
}

private fun ChannelUserBannedEventDto.toDomain(): ChannelUserBannedEvent {
    return ChannelUserBannedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
        expiration = expiration,
        shadow = shadow ?: false,
    )
}

private fun GlobalUserBannedEventDto.toDomain(): GlobalUserBannedEvent {
    return GlobalUserBannedEvent(
        type = type,
        user = user.toDomain(),
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
    )
}

private fun UserDeletedEventDto.toDomain(): UserDeletedEvent {
    return UserDeletedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
    )
}

private fun UserPresenceChangedEventDto.toDomain(): UserPresenceChangedEvent {
    return UserPresenceChangedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
    )
}

private fun UserStartWatchingEventDto.toDomain(): UserStartWatchingEvent {
    return UserStartWatchingEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        watcherCount = watcher_count,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
    )
}

private fun UserStopWatchingEventDto.toDomain(): UserStopWatchingEvent {
    return UserStopWatchingEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        watcherCount = watcher_count,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
    )
}

private fun ChannelUserUnbannedEventDto.toDomain(): ChannelUserUnbannedEvent {
    return ChannelUserUnbannedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
    )
}

private fun GlobalUserUnbannedEventDto.toDomain(): GlobalUserUnbannedEvent {
    return GlobalUserUnbannedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
    )
}

private fun UserUpdatedEventDto.toDomain(): UserUpdatedEvent {
    return UserUpdatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(),
    )
}

fun DownstreamMessageDto.toDomain(): Message =
    Message(
        attachments = attachments.mapTo(mutableListOf(), AttachmentDto::toDomain),
        channelInfo = channel?.toDomain(),
        cid = cid,
        command = command,
        createdAt = created_at,
        deletedAt = deleted_at,
        html = html,
        i18n = i18n,
        id = id,
        latestReactions = latest_reactions.mapTo(mutableListOf(), DownstreamReactionDto::toDomain),
        mentionedUsers = mentioned_users.mapTo(mutableListOf(), DownstreamUserDto::toDomain),
        ownReactions = own_reactions.mapTo(mutableListOf(), DownstreamReactionDto::toDomain),
        parentId = parent_id,
        pinExpires = pin_expires,
        pinned = pinned,
        pinnedAt = pinned_at,
        pinnedBy = pinned_by?.toDomain(),
        reactionCounts = reaction_counts.orEmpty().toMutableMap(),
        reactionScores = reaction_scores.orEmpty().toMutableMap(),
        reactionGroups = reaction_groups.orEmpty().mapValues { it.value.toDomain(it.key) },
        replyCount = reply_count,
        deletedReplyCount = deleted_reply_count,
        replyMessageId = quoted_message_id,
        replyTo = quoted_message?.toDomain(),
        shadowed = shadowed,
        showInChannel = show_in_channel,
        silent = silent,
        text = text,
        threadParticipants = thread_participants.map(DownstreamUserDto::toDomain),
        type = type,
        updatedAt = updated_at,
        user = user.toDomain(),
        moderationDetails = moderation_details?.toDomain(),
        messageTextUpdatedAt = message_text_updated_at,
        extraData = extraData.toMutableMap(),
    )

private fun ConnectedEventDto.toDomain(): ConnectedEvent {
    return ConnectedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        me = me.toDomain(),
        connectionId = connection_id,
    )
}

private fun ConnectionErrorEventDto.toDomain(): ConnectionErrorEvent {
    return ConnectionErrorEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        connectionId = connection_id,
        error = error.toDomain(),
    )
}

private fun ConnectingEventDto.toDomain(): ConnectingEvent {
    return ConnectingEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
    )
}

private fun DisconnectedEventDto.toDomain(): DisconnectedEvent {
    return DisconnectedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
    )
}

fun ErrorEventDto.toDomain(): ErrorEvent {
    return ErrorEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        error = error,
    )
}

fun UnknownEventDto.toDomain(): UnknownEvent {
    return UnknownEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user?.toDomain(),
        rawData = rawData,
    )
}
internal fun DownstreamUserDto.toDomain(): User =
    User(
        id = id,
        name = name ?: "",
        image = image ?: "",
        role = role,
        invisible = invisible,
        language = language ?: "",
        banned = banned,
        devices = devices.orEmpty().map(DeviceDto::toDomain),
        online = online,
        createdAt = created_at,
        deactivatedAt = deactivated_at,
        updatedAt = updated_at,
        lastActive = last_active,
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
        mutes = mutes.orEmpty().map(DownstreamMuteDto::toDomain),
        teams = teams,
        channelMutes = channel_mutes.orEmpty().map(DownstreamChannelMuteDto::toDomain),
        extraData = extraData.toMutableMap(),
    )

fun DownstreamMemberDto.toDomain(): Member =
    Member(
        user = user.toDomain(),
        createdAt = created_at,
        updatedAt = updated_at,
        isInvited = invited,
        inviteAcceptedAt = invite_accepted_at,
        inviteRejectedAt = invite_rejected_at,
        shadowBanned = shadow_banned,
        banned = banned,
        channelRole = channel_role,
        notificationsMuted = notifications_muted,
        status = status,
    )

fun Member.toDto(): UpstreamMemberDto =
    UpstreamMemberDto(
        user = user.toDto(),
        created_at = createdAt,
        updated_at = updatedAt,
        invited = isInvited,
        invite_accepted_at = inviteAcceptedAt,
        invite_rejected_at = inviteRejectedAt,
        shadow_banned = shadowBanned,
        banned = banned,
        channel_role = channelRole,
        notifications_muted = notificationsMuted,
        status = status,
    )

fun DownstreamModerationDetailsDto.toDomain(): MessageModerationDetails {
    return MessageModerationDetails(
        originalText = original_text.orEmpty(),
        action = MessageModerationAction.fromRawValue(action.orEmpty()),
        errorMsg = error_msg.orEmpty(),
    )
}

fun DownstreamReactionGroupDto.toDomain(type: String): ReactionGroup =
    ReactionGroup(
        type = type,
        count = count,
        sumScore = sum_scores,
        firstReactionAt = first_reaction_at,
        lastReactionAt = last_reaction_at,
    )

fun DownstreamReactionDto.toDomain(): Reaction =
    Reaction(
        createdAt = created_at,
        messageId = message_id,
        score = score,
        type = type,
        updatedAt = updated_at,
        user = user?.toDomain(),
        userId = user_id,
        extraData = extraData.toMutableMap(),
    )

fun DownstreamMuteDto.toDomain(): Mute =
    Mute(
        user = user.toDomain(),
        target = target.toDomain(),
        createdAt = created_at,
        updatedAt = updated_at,
        expires = expires,
    )

fun SocketErrorResponse.toDomain(): SocketErrorMessage {
    return SocketErrorMessage(
        error = error?.toDomain(),
    )
}

fun SocketErrorResponse.ErrorResponse.toDomain(): ErrorResponse {
    val dto = this
    return ErrorResponse(
        code = dto.code,
        message = dto.message,
        statusCode = dto.StatusCode,
        exceptionFields = dto.exception_fields,
        moreInfo = dto.more_info,
        details = dto.details.map { it.toDomain() },
    ).apply {
        duration = dto.duration
    }
}

fun SocketErrorResponse.ErrorResponse.ErrorDetail.toDomain(): ErrorDetail {
    val dto = this
    return ErrorDetail(
        code = dto.code,
        messages = dto.messages,
    )
}

fun CreateVideoCallResponse.toDomain(): VideoCallInfo {
    return VideoCallInfo(
        callId = call.id,
        provider = call.provider,
        type = call.type,
        agoraChannel = call.agora.toDomain(),
        hmsRoom = call.hms.toDomain(),
        videoCallToken = VideoCallToken(
            token = token,
            agoraUid = agoraUid,
            agoraAppId = agoraAppId,
        ),
    )
}

fun AgoraDto.toDomain(): AgoraChannel {
    return AgoraChannel(channel = channel)
}

fun HMSDto.toDomain(): HMSRoom {
    return HMSRoom(roomId = roomId, roomName = roomName)
}

fun SearchWarningDto.toDomain(): SearchWarning {
    return SearchWarning(
        channelSearchCids = channel_search_cids,
        channelSearchCount = channel_search_count,
        warningCode = warning_code,
        warningDescription = warning_description,
    )
}

fun DownstreamFlagDto.toDomain(): Flag {
    return Flag(
        user = user.toDomain(),
        targetUser = target_user?.toDomain(),
        targetMessageId = target_message_id,
        reviewedBy = created_at,
        createdByAutomod = created_by_automod,
        createdAt = approved_at,
        updatedAt = updated_at,
        reviewedAt = reviewed_at,
        approvedAt = approved_at,
        rejectedAt = rejected_at,
    )
}

fun VideoCallTokenResponse.toDomain(): VideoCallToken {
    return VideoCallToken(
        token = token,
        agoraUid = agoraUid,
        agoraAppId = agoraAppId,
    )
}
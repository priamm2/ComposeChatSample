package com.example.composechatsample.core

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.text.format.DateUtils
import android.webkit.MimeTypeMap
import androidx.annotation.FloatRange
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import coil.ImageLoader
import coil.imageLoader
import com.example.composechatsample.R
import com.example.composechatsample.common.MediaGalleryPreviewActivityAttachmentState
import com.example.composechatsample.common.MediaGalleryPreviewActivityState
import com.example.composechatsample.common.StreamCdnImageResizing
import com.example.composechatsample.core.api.AnyChannelPaginationRequest
import com.example.composechatsample.core.api.QueryChannelRequest
import com.example.composechatsample.core.errors.ChatErrorCode
import com.example.composechatsample.core.events.ChannelTruncatedEvent
import com.example.composechatsample.core.events.ChannelUpdatedByUserEvent
import com.example.composechatsample.core.events.ChannelUpdatedEvent
import com.example.composechatsample.core.events.ChatEvent
import com.example.composechatsample.core.events.MarkAllReadEvent
import com.example.composechatsample.core.events.MessageDeletedEvent
import com.example.composechatsample.core.events.MessageReadEvent
import com.example.composechatsample.core.events.MessageUpdatedEvent
import com.example.composechatsample.core.events.NewMessageEvent
import com.example.composechatsample.core.events.NotificationMarkReadEvent
import com.example.composechatsample.core.events.NotificationMarkUnreadEvent
import com.example.composechatsample.core.events.NotificationMessageNewEvent
import com.example.composechatsample.core.events.ReactionDeletedEvent
import com.example.composechatsample.core.events.ReactionNewEvent
import com.example.composechatsample.core.events.ReactionUpdateEvent
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.ChannelUserRead
import com.example.composechatsample.core.models.Device
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.PushProvider
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.UploadedFile
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.response.UploadFileResponse
import com.example.composechatsample.core.push.PushDevice
import com.example.composechatsample.log.taggedLogger
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.net.UnknownHostException
import java.util.Date
import java.util.UUID
import java.util.regex.Pattern
import kotlin.properties.ReadOnlyProperty
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.api.QueryChannelsPaginationRequest
import com.example.composechatsample.core.events.ConnectedEvent
import com.example.composechatsample.core.models.AttachmentType
import com.example.composechatsample.core.models.ChannelCapabilities
import com.example.composechatsample.core.models.ChannelConfig
import com.example.composechatsample.core.models.ChannelInfo
import com.example.composechatsample.core.models.Command
import com.example.composechatsample.core.models.Config
import com.example.composechatsample.core.models.MessageModerationAction
import com.example.composechatsample.core.models.MessageModerationDetails
import com.example.composechatsample.core.models.MessageType
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.models.TimeDuration
import com.example.composechatsample.core.models.mapper.syncUnreadCountWithReads
import com.example.composechatsample.core.models.streamcdn.image.StreamCdnCropImageMode
import com.example.composechatsample.core.models.streamcdn.image.StreamCdnOriginalImageDimensions
import com.example.composechatsample.core.models.streamcdn.image.StreamCdnResizeImageMode
import com.example.composechatsample.core.plugin.LogicRegistry
import com.example.composechatsample.core.plugin.MutableGlobalState
import com.example.composechatsample.core.plugin.QueryChannelsMutableState
import com.example.composechatsample.core.repository.SyncState
import com.example.composechatsample.core.state.StatePlugin
import com.example.composechatsample.core.state.StateRegistry
import com.example.composechatsample.data.AttachmentEntity
import com.example.composechatsample.data.ChannelConfigEntity
import com.example.composechatsample.data.ChannelConfigInnerEntity
import com.example.composechatsample.data.ChannelEntity
import com.example.composechatsample.data.ChannelInfoEntity
import com.example.composechatsample.data.ChannelUserReadEntity
import com.example.composechatsample.data.CommandInnerEntity
import com.example.composechatsample.data.MemberEntity
import com.example.composechatsample.data.MessageEntity
import com.example.composechatsample.data.MessageInnerEntity
import com.example.composechatsample.data.ModerationDetailsEntity
import com.example.composechatsample.data.PrivacySettingsEntity
import com.example.composechatsample.data.ReactionEntity
import com.example.composechatsample.data.ReadReceiptsEntity
import com.example.composechatsample.data.ReplyAttachmentEntity
import com.example.composechatsample.data.ReplyMessageEntity
import com.example.composechatsample.data.ReplyMessageInnerEntity
import com.example.composechatsample.data.SyncStateEntity
import com.example.composechatsample.data.TypingIndicatorsEntity
import com.example.composechatsample.data.UploadStateEntity
import com.example.composechatsample.data.UploadStateEntity.Companion.UPLOAD_STATE_FAILED
import com.example.composechatsample.data.UploadStateEntity.Companion.UPLOAD_STATE_IN_PROGRESS
import com.example.composechatsample.data.UploadStateEntity.Companion.UPLOAD_STATE_SUCCESS
import com.example.composechatsample.log.StreamLog
import com.example.composechatsample.screen.MessageItemState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.abs

fun MediaGalleryPreviewActivityState.toMessage(): Message =
    Message(
        id = this.messageId,
        user = User(
            id = this.userId,
            name = this.userName,
            image = this.userImage,
        ),
        attachments = this.attachments.map { it.toAttachment() }.toMutableList(),
    )
fun MediaGalleryPreviewActivityAttachmentState.toAttachment(): Attachment = Attachment(
    name = this.name,
    url = this.url,
    thumbUrl = this.thumbUrl,
    imageUrl = this.imageUrl,
    assetUrl = this.assetUrl,
    originalWidth = this.originalWidth,
    originalHeight = this.originalHeight,
    type = this.type,
)


fun Channel.toEntity(): ChannelEntity {
    return ChannelEntity(
        type = type,
        channelId = id,
        name = name,
        image = image,
        cooldown = cooldown,
        frozen = frozen,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        extraData = extraData,
        syncStatus = syncStatus,
        hidden = hidden,
        hideMessagesBefore = hiddenMessagesBefore,
        members = members.map(Member::toEntity).associateBy(MemberEntity::userId).toMutableMap(),
        memberCount = memberCount,
        reads = read.map(ChannelUserRead::toEntity).associateBy(ChannelUserReadEntity::userId).toMutableMap(),
        lastMessageId = lastMessage?.id,
        lastMessageAt = lastMessageAt,
        createdByUserId = createdBy.id,
        watcherIds = watchers.map(User::id),
        watcherCount = watcherCount,
        team = team,
        ownCapabilities = ownCapabilities,
        membership = membership?.toEntity(),
    )
}

suspend fun ChannelEntity.toModel(
    getUser: suspend (userId: String) -> User,
    getMessage: suspend (messageId: String) -> Message?,
): Channel = Channel(
    cooldown = cooldown,
    type = type,
    id = channelId,
    name = name,
    image = image,
    frozen = frozen,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    extraData = extraData.toMutableMap(),
    lastMessageAt = lastMessageAt,
    syncStatus = syncStatus,
    hidden = hidden,
    hiddenMessagesBefore = hideMessagesBefore,
    members = members.values.map { it.toModel(getUser) },
    memberCount = memberCount,
    messages = listOfNotNull(lastMessageId?.let { getMessage(it) }),
    read = reads.values.map { it.toModel(getUser) },
    createdBy = getUser(createdByUserId),
    watchers = watcherIds.map { getUser(it) },
    watcherCount = watcherCount,
    team = team,
    ownCapabilities = ownCapabilities,
    membership = membership?.toModel(getUser),
).syncUnreadCountWithReads()

fun maxOf(vararg dates: Date?): Date? = dates.reduceOrNull { acc, date -> max(acc, date) }

public fun max(dateA: Date?, dateB: Date?): Date? = when (dateA after dateB) {
    true -> dateA
    else -> dateB
}

fun String.applyStreamCdnImageResizingIfEnabled(streamCdnImageResizing: StreamCdnImageResizing): String =
    if (streamCdnImageResizing.imageResizingEnabled) {
        createResizedStreamCdnImageUrl(
            resizedHeightPercentage = streamCdnImageResizing.resizedWidthPercentage,
            resizedWidthPercentage = streamCdnImageResizing.resizedHeightPercentage,
            resizeMode = streamCdnImageResizing.resizeMode,
            cropMode = streamCdnImageResizing.cropMode,
        )
    } else {
        this
    }

val LocalStreamImageLoader: StreamImageLoaderProvidableCompositionLocal =
    StreamImageLoaderProvidableCompositionLocal()

@JvmInline
public value class StreamImageLoaderProvidableCompositionLocal internal constructor(
    private val delegate: ProvidableCompositionLocal<ImageLoader?> = staticCompositionLocalOf { null },
) {

    public val current: ImageLoader
        @Composable
        @ReadOnlyComposable
        get() = delegate.current ?: LocalContext.current.imageLoader

    public infix fun provides(value: ImageLoader): ProvidedValue<ImageLoader?> = delegate provides value
}

val ChatClient.state: StateRegistry
    @Throws(IllegalArgumentException::class)
    get() = resolveDependency<StatePlugin, StateRegistry>()

public fun String.createResizedStreamCdnImageUrl(
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) resizedWidthPercentage: Float,
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) resizedHeightPercentage: Float,
    resizeMode: StreamCdnResizeImageMode? = null,
    cropMode: StreamCdnCropImageMode? = null,
): String {
    val logger = StreamLog.getLogger("Chat:resizedStreamCdnImageUrl")

    val streamCdnImageDimensions = this.getStreamCdnHostedImageDimensions()

    return if (streamCdnImageDimensions != null) {
        val imageLinkUri = this.toUri()

        if (imageLinkUri.wasImagePreviouslyResized()) {
            logger.w {
                "Image URL already contains resizing parameters. Please apply resizing parameters only to " +
                        "original image URLs."
            }

            return this@createResizedStreamCdnImageUrl
        }

        val resizedWidth: Int = (streamCdnImageDimensions.originalWidth * resizedWidthPercentage).toInt()
        val resizedHeight: Int = (streamCdnImageDimensions.originalHeight * resizedHeightPercentage).toInt()

        val resizedImageUrl = imageLinkUri
            .buildUpon()
            .appendValueAsQueryParameterIfNotNull(
                key = StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_RESIZED_WIDTH,
                value = resizedWidth,
            )
            .appendValueAsQueryParameterIfNotNull(
                key = StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_RESIZED_HEIGHT,
                value = resizedHeight,
            )
            .appendValueAsQueryParameterIfNotNull(
                key = StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_RESIZE_MODE,
                value = resizeMode?.queryParameterName,
            )
            .appendValueAsQueryParameterIfNotNull(
                key = StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_CROP_MODE,
                value = cropMode?.queryParameterName,
            )
            .build()
            .toString()

        logger.i {
            "Resized Stream CDN hosted image URL: $resizedImageUrl"
        }

        resizedImageUrl
    } else {
        logger.i {
            "Image not hosted by Stream's CDN or not containing original width and height query parameters " +
                    "was not resized"
        }
        this
    }
}

fun Uri.Builder.appendValueAsQueryParameterIfNotNull(key: String, value: Int?): Uri.Builder {
    return if (value != null) {
        this.appendQueryParameter(key, value.toString())
    } else {
        this
    }
}

fun Channel.isOneToOne(currentUser: User?): Boolean {
    return isDistinct() &&
            members.size == 2 &&
            members.any { it.user.id == currentUser?.id }
}

fun Channel.getMembersStatusText(context: Context, currentUser: User?): String {
    return getMembersStatusText(
        context = context,
        currentUser = currentUser,
        userOnlineResId = R.string.stream_compose_user_status_online,
        userLastSeenJustNowResId = R.string.stream_compose_user_status_last_seen_just_now,
        userLastSeenResId = R.string.stream_compose_user_status_last_seen,
        memberCountResId = R.plurals.stream_compose_member_count,
        memberCountWithOnlineResId = R.string.stream_compose_member_count_online,
    )
}

fun Channel.getMembersStatusText(
    context: Context,
    currentUser: User?,
    @StringRes userOnlineResId: Int,
    @StringRes userLastSeenJustNowResId: Int,
    @StringRes userLastSeenResId: Int,
    @PluralsRes memberCountResId: Int,
    @StringRes memberCountWithOnlineResId: Int,
): String {
    return when {
        isOneToOne(currentUser) -> members.first { it.user.id != currentUser?.id }
            .user
            .getLastSeenText(
                context = context,
                userOnlineResId = userOnlineResId,
                userLastSeenJustNowResId = userLastSeenJustNowResId,
                userLastSeenResId = userLastSeenResId,
            )
        else -> {
            val memberCountString = context.resources.getQuantityString(
                memberCountResId,
                memberCount,
                memberCount,
            )

            return if (watcherCount > 0) {
                context.getString(
                    memberCountWithOnlineResId,
                    memberCountString,
                    watcherCount,
                )
            } else {
                memberCountString
            }
        }
    }
}

fun User.getLastSeenText(
    context: Context,
    @StringRes userOnlineResId: Int,
    @StringRes userLastSeenJustNowResId: Int,
    @StringRes userLastSeenResId: Int,
): String {
    if (online) {
        return context.getString(userOnlineResId)
    }

    return (lastActive ?: updatedAt ?: createdAt)?.let {
        if (it.isInLastMinute()) {
            context.getString(userLastSeenJustNowResId)
        } else {
            context.getString(
                userLastSeenResId,
                DateUtils.getRelativeTimeSpanString(it.time).toString(),
            )
        }
    } ?: ""
}

fun Attachment.isGiphy(): Boolean = type == AttachmentType.GIPHY

fun Attachment.hasLink(): Boolean = titleLink != null || ogUrl != null

val User.initials: String
    get() = name.initials()

val Channel.initials: String
    get() = name.initials()

fun String.initials(): String {
    return trim()
        .split("\\s+".toRegex())
        .take(2)
        .joinToString(separator = "") { it.take(1).uppercase() }
}

fun Date.isInLastMinute(): Boolean = (Date().time - 60000 < time)

fun Channel.isDistinct(): Boolean = cid.contains("!members")

fun ChannelCapabilities.toSet(): Set<String> = setOf(
    BAN_CHANNEL_MEMBERS,
    CONNECT_EVENTS,
    DELETE_ANY_MESSAGE,
    DELETE_CHANNEL,
    DELETE_OWN_MESSAGE,
    FLAG_MESSAGE,
    FREEZE_CHANNEL,
    LEAVE_CHANNEL,
    MUTE_CHANNEL,
    PIN_MESSAGE,
    QUOTE_MESSAGE,
    READ_EVENTS,
    SEARCH_MESSAGES,
    SEND_CUSTOM_EVENTS,
    SEND_LINKS,
    SEND_MESSAGE,
    SEND_REACTION,
    SEND_REPLY,
    SET_CHANNEL_COOLDOWN,
    SEND_TYPING_EVENTS,
    TYPING_EVENTS,
    UPDATE_ANY_MESSAGE,
    UPDATE_CHANNEL,
    UPDATE_CHANNEL_MEMBERS,
    UPDATE_OWN_MESSAGE,
    UPLOAD_FILE,
)

fun Uri.Builder.appendValueAsQueryParameterIfNotNull(key: String, value: String?): Uri.Builder {
    return if (value != null) {
        this.appendQueryParameter(key, value)
    } else {
        this
    }
}

fun Uri.wasImagePreviouslyResized(): Boolean =
    queryParameterNames.intersect(
        listOf(
            StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_RESIZED_WIDTH,
            StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_RESIZED_HEIGHT,
            StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_RESIZE_MODE,
            StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_CROP_MODE,
        ),
    ).isNotEmpty()

fun String.getStreamCdnHostedImageDimensions(): StreamCdnOriginalImageDimensions? {
    return try {
        val imageUri = this.toUri()

        val width = imageUri.getQueryParameter("ow")
            ?.toInt()

        val height = imageUri.getQueryParameter("oh")
            ?.toInt()

        if (height != null && width != null) {
            StreamCdnOriginalImageDimensions(
                originalWidth = width,
                originalHeight = height,
            )
        } else {
            null
        }
    } catch (e: java.lang.Exception) {
        val logger = StreamLog.getLogger("Chat: getStreamCDNHostedImageDimensions")
        logger.e { "Failed to parse Stream CDN image dimensions from the URL:\n ${e.stackTraceToString()}" }

        null
    }
}

suspend fun ReplyMessageEntity.toModel(
    getUser: suspend (userId: String) -> User,
): Message {
    val entity = this
    return this.replyMessageInnerEntity.run {
        Message(
            id = id,
            cid = cid,
            user = getUser(userId),
            text = text,
            html = html,
            attachments = entity.attachments.map { it.toModel() }.toMutableList(),
            type = type,
            replyCount = replyCount,
            deletedReplyCount = deletedReplyCount,
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedLocallyAt,
            deletedAt = deletedAt,
            parentId = parentId,
            command = command,
            syncStatus = syncStatus,
            shadowed = shadowed,
            i18n = i18n,
            latestReactions = mutableListOf(),
            ownReactions = mutableListOf(),
            mentionedUsers = remoteMentionedUserIds.map { getUser(it) }.toMutableList(),
            mentionedUsersIds = mentionedUsersId.toMutableList(),
            replyTo = null,
            replyMessageId = null,
            threadParticipants = threadParticipantsIds.map { getUser(it) },
            showInChannel = showInChannel,
            silent = silent,
            pinned = pinned,
            pinnedAt = pinnedAt,
            pinExpires = pinExpires,
            pinnedBy = pinnedByUserId?.let { getUser(it) },
            moderationDetails = moderationDetails?.toModel(),
            messageTextUpdatedAt = messageTextUpdatedAt,
        )
    }
}

fun ModerationDetailsEntity.toModel(): MessageModerationDetails {
    return MessageModerationDetails(
        originalText = originalText,
        action = MessageModerationAction.fromRawValue(action),
        errorMsg = errorMsg,
    )
}

fun Message.toEntity(): MessageEntity = MessageEntity(
    messageInnerEntity = MessageInnerEntity(
        id = id,
        cid = cid,
        userId = user.id,
        text = text,
        html = html,
        syncStatus = syncStatus,
        type = type,
        replyCount = replyCount,
        deletedReplyCount = deletedReplyCount,
        createdAt = createdAt,
        createdLocallyAt = createdLocallyAt,
        updatedAt = updatedAt,
        updatedLocallyAt = updatedLocallyAt,
        deletedAt = deletedAt,
        parentId = parentId,
        command = command,
        extraData = extraData,
        reactionCounts = reactionCounts,
        reactionScores = reactionScores,
        shadowed = shadowed,
        i18n = i18n,
        remoteMentionedUserIds = mentionedUsers.map(User::id),
        mentionedUsersId = mentionedUsersIds,
        replyToId = replyTo?.id ?: replyMessageId,
        threadParticipantsIds = threadParticipants.map(User::id),
        showInChannel = showInChannel,
        silent = silent,
        channelInfo = channelInfo?.toEntity(),
        pinned = pinned,
        pinnedAt = pinnedAt,
        pinExpires = pinExpires,
        pinnedByUserId = pinnedBy?.id,
        skipPushNotification = skipPushNotification,
        skipEnrichUrl = skipEnrichUrl,
        moderationDetails = moderationDetails?.toEntity(),
        messageTextUpdatedAt = messageTextUpdatedAt,
    ),
    attachments = attachments.mapIndexed { index, attachment -> attachment.toEntity(id, index) },
    latestReactions = latestReactions.map(Reaction::toEntity),
    ownReactions = ownReactions.map(Reaction::toEntity),
)

suspend fun MessageEntity.toModel(
    getUser: suspend (userId: String) -> User,
    getReply: suspend (messageId: String) -> Message?,
): Message = with(messageInnerEntity) {
    Message(
        id = id,
        cid = cid,
        user = getUser(userId),
        text = text,
        html = html,
        attachments = attachments.map(AttachmentEntity::toModel).toMutableList(),
        type = type,
        replyCount = replyCount,
        deletedReplyCount = deletedReplyCount,
        createdAt = createdAt,
        createdLocallyAt = createdLocallyAt,
        updatedAt = updatedAt,
        updatedLocallyAt = updatedLocallyAt,
        deletedAt = deletedAt,
        parentId = parentId,
        command = command,
        extraData = extraData.toMutableMap(),
        reactionCounts = reactionCounts.toMutableMap(),
        reactionScores = reactionScores.toMutableMap(),
        syncStatus = syncStatus,
        shadowed = shadowed,
        i18n = i18n,
        latestReactions = (latestReactions.map { it.toModel(getUser) }).toMutableList(),
        ownReactions = (ownReactions.map { it.toModel(getUser) }).toMutableList(),
        mentionedUsers = remoteMentionedUserIds.map { getUser(it) }.toMutableList(),
        mentionedUsersIds = mentionedUsersId.toMutableList(),
        replyTo = replyToId?.let { getReply(it) },
        replyMessageId = replyToId,
        threadParticipants = threadParticipantsIds.map { getUser(it) },
        showInChannel = showInChannel,
        silent = silent,
        channelInfo = channelInfo?.toModel(),
        pinned = pinned,
        pinnedAt = pinnedAt,
        pinExpires = pinExpires,
        pinnedBy = pinnedByUserId?.let { getUser(it) },
        skipEnrichUrl = skipEnrichUrl,
        skipPushNotification = skipPushNotification,
        moderationDetails = moderationDetails?.toModel(),
        messageTextUpdatedAt = messageTextUpdatedAt,
    )
}

fun PrivacySettings.toEntity(): PrivacySettingsEntity {
    return PrivacySettingsEntity(
        typingIndicators = typingIndicators?.let {
            TypingIndicatorsEntity(
                enabled = it.enabled,
            )
        },
        readReceipts = readReceipts?.let {
            ReadReceiptsEntity(
                enabled = it.enabled,
            )
        },
    )
}

fun Channel.getLastMessage(currentUser: User?): Message? = getPreviewMessage(currentUser)


fun Channel.getPreviewMessage(currentUser: User?): Message? =
    if (isInsideSearch) {
        cachedLatestMessages
    } else {
        messages
    }.asSequence()
        .filter { it.createdAt != null || it.createdLocallyAt != null }
        .filter { it.deletedAt == null }
        .filter { !it.silent }
        .filter { it.user.id == currentUser?.id || !it.shadowed }
        .filter { it.isRegular() || it.isSystem() }
        .maxByOrNull { requireNotNull(it.createdAt ?: it.createdLocallyAt) }

fun Channel.getDisplayName(
    context: Context,
    currentUser: User? = ChatClient.instance().clientState.user.value,
    @StringRes fallback: Int,
    maxMembers: Int = 2,
): String {
    return name.takeIf { it.isNotEmpty() }
        ?: nameFromMembers(context, currentUser, maxMembers)
        ?: context.getString(fallback)
}

private fun Channel.nameFromMembers(
    context: Context,
    currentUser: User?,
    maxMembers: Int,
): String? {
    val users = getUsersExcludingCurrent(currentUser)
    return when {
        users.isNotEmpty() -> {
            val usersCount = users.size
            val userNames = users
                .sortedBy(User::name)
                .take(maxMembers)
                .joinToString { it.name }
            when (usersCount <= maxMembers) {
                true -> userNames
                else -> {
                    context.getString(
                        R.string.stream_ui_channel_list_untitled_channel_plus_more,
                        userNames,
                        usersCount - maxMembers,
                    )
                }
            }
        }
        members.size == 1 -> members.first().user.name

        else -> null
    }
}

fun <T> StateFlow<T>.asState(coroutineScope: CoroutineScope): State<T> {
    val state = mutableStateOf(this.value)
    onEach { state.value = it }.launchIn(coroutineScope)
    return state
}

fun Message.isEphemeral(): Boolean = type == MessageType.EPHEMERAL

fun Message.isGiphy(): Boolean = command == AttachmentType.GIPHY


fun Message.isGiphyEphemeral(): Boolean = isGiphy() && isEphemeral()

fun Message.isFailed(): Boolean = this.syncStatus == SyncStatus.FAILED_PERMANENTLY

fun Message.isErrorOrFailed(): Boolean = isError() || isFailed()

fun MessageItemState.isErrorOrFailed(): Boolean = isMine && message.isErrorOrFailed()


fun Message.isEmojiOnlyWithoutBubble(): Boolean = isFewEmoji() &&
        replyTo == null

fun Message.getEmojiCount(): Int = EmojiUtil.getEmojiCount(this)

fun Message.isEmojiOnly(): Boolean = EmojiUtil.isEmojiOnly(this)

fun Message.isFewEmoji(): Boolean = isEmojiOnly() && getEmojiCount() <= 3
fun Message.isSingleEmoji(): Boolean = EmojiUtil.isSingleEmoji(this)
val Attachment.imagePreviewUrl: String?
    get() = thumbUrl ?: imageUrl
internal fun <T> Flow<T>.asState(coroutineScope: CoroutineScope, defaultValue: T): State<T> {
    val state = mutableStateOf(defaultValue)
    onEach { state.value = it }.launchIn(coroutineScope)
    return state
}

fun String.addSchemeToUrlIfNeeded(): String = when {
    this.startsWith("mailto:") -> this
    this.startsWith("http://") -> this
    this.startsWith("https://") -> this
    else -> "http://$this"
}


fun Channel.getUsersExcludingCurrent(
    currentUser: User? = ChatClient.instance().getCurrentUser(),
): List<User> = getMembersExcludingCurrent(currentUser).map { it.user }

fun Channel.getMembersExcludingCurrent(
    currentUser: User? = ChatClient.instance().getCurrentUser(),
): List<Member> =
    members.filter { it.user.id != currentUser?.id }

public fun Message.isThreadStart(): Boolean = threadParticipants.isNotEmpty()

fun Message.isUploading(): Boolean = attachments.any { it.isUploading() }

fun Attachment.isUploading(): Boolean {
    return (uploadState is Attachment.UploadState.InProgress || uploadState is Attachment.UploadState.Idle) &&
            upload != null &&
            uploadId != null
}

fun Channel.getReadStatuses(userToIgnore: User?): List<Date> {
    return read.filter { it.user.id != userToIgnore?.id }
        .map { it.lastRead }
}

fun Message.getCreatedAtOrThrow(): Date {
    val created = getCreatedAtOrNull()
    return checkNotNull(created) { "a message needs to have a non null value for either createdAt or createdLocallyAt" }
}

fun Message.isSystem(): Boolean = type == MessageType.SYSTEM

fun Message.isRegular(): Boolean = type == MessageType.REGULAR

fun PrivacySettingsEntity.toModel(): PrivacySettings {
    return PrivacySettings(
        typingIndicators = typingIndicators?.let {
            TypingIndicators(
                enabled = it.enabled,
            )
        },
        readReceipts = readReceipts?.let {
            ReadReceipts(
                enabled = it.enabled,
            )
        },
    )
}

fun SyncStateEntity.toModel() =
    SyncState(userId, activeChannelIds, lastSyncedAt, rawLastSyncedAt, markedAllReadAt)

fun SyncState.toEntity() =
    SyncStateEntity(userId, activeChannelIds, lastSyncedAt, rawLastSyncedAt, markedAllReadAt)

fun ChannelInfoEntity.toModel(): ChannelInfo = ChannelInfo(
    cid = cid,
    id = id,
    type = type,
    memberCount = memberCount ?: 0,
    name = name,
)

suspend fun ReactionEntity.toModel(getUser: suspend (userId: String) -> User): Reaction = Reaction(
    messageId = messageId,
    type = type,
    score = score,
    user = getUser(userId),
    extraData = extraData.toMutableMap(),
    createdAt = createdAt,
    createdLocallyAt = createdLocallyAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    syncStatus = syncStatus,
    userId = userId,
    enforceUnique = enforceUnique,
)

fun AttachmentEntity.toModel(): Attachment = Attachment(
    authorName = authorName,
    titleLink = titleLink,
    authorLink = authorLink,
    thumbUrl = thumbUrl,
    imageUrl = imageUrl,
    assetUrl = assetUrl,
    ogUrl = ogUrl,
    mimeType = mimeType,
    fileSize = fileSize,
    title = title,
    text = text,
    type = type,
    image = image,
    url = url,
    name = name,
    fallback = fallback,
    upload = uploadFilePath?.let(::File),
    uploadState = uploadState?.toModel(uploadFilePath?.let(::File)),
    originalHeight = originalHeight,
    originalWidth = originalWidth,
    extraData = extraData,
)

fun Message.toReplyEntity(): ReplyMessageEntity =
    ReplyMessageEntity(
        replyMessageInnerEntity = ReplyMessageInnerEntity(
            id = id,
            cid = cid,
            userId = user.id,
            text = text,
            html = html,
            syncStatus = syncStatus,
            type = type,
            replyCount = replyCount,
            deletedReplyCount = deletedReplyCount,
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedLocallyAt,
            deletedAt = deletedAt,
            parentId = parentId,
            command = command,
            shadowed = shadowed,
            i18n = i18n,
            remoteMentionedUserIds = mentionedUsers.map(User::id),
            mentionedUsersId = mentionedUsersIds,
            threadParticipantsIds = threadParticipants.map(User::id),
            showInChannel = showInChannel,
            silent = silent,
            pinned = pinned,
            pinnedAt = pinnedAt,
            pinExpires = pinExpires,
            pinnedByUserId = pinnedBy?.id,
            moderationDetails = moderationDetails?.toEntity(),
        ),
        attachments = attachments.mapIndexed { index, attachment -> attachment.toReplyEntity(id, index) },
    )

fun Attachment.toReplyEntity(messageId: String, index: Int): ReplyAttachmentEntity {
    val mutableExtraData = extraData.toMutableMap()
    val generatedId = mutableExtraData.getOrPut(AttachmentEntity.EXTRA_DATA_ID_KEY) {
        AttachmentEntity.generateId(messageId, index)
    } as String
    return ReplyAttachmentEntity(
        id = generatedId,
        messageId = messageId,
        authorName = authorName,
        titleLink = titleLink,
        authorLink = authorLink,
        thumbUrl = thumbUrl,
        imageUrl = imageUrl,
        assetUrl = assetUrl,
        ogUrl = ogUrl,
        mimeType = mimeType,
        fileSize = fileSize,
        title = title,
        text = text,
        type = type,
        image = image,
        url = url,
        name = name,
        fallback = fallback,
        uploadFilePath = upload?.absolutePath,
        uploadState = uploadState?.toEntity(),
        originalHeight = originalHeight,
        originalWidth = originalWidth,
        extraData = mutableExtraData,
    )
}

fun Reaction.toEntity(): ReactionEntity = ReactionEntity(
    messageId = messageId,
    userId = fetchUserId(),
    type = type,
    score = score,
    createdAt = createdAt,
    createdLocallyAt = createdLocallyAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    extraData = extraData,
    syncStatus = syncStatus,
    enforceUnique = enforceUnique,
)

fun Attachment.toEntity(messageId: String, index: Int): AttachmentEntity {
    val mutableExtraData = extraData.toMutableMap()
    val generatedId = mutableExtraData.getOrPut(AttachmentEntity.EXTRA_DATA_ID_KEY) {
        AttachmentEntity.generateId(messageId, index)
    } as String
    return AttachmentEntity(
        id = generatedId,
        messageId = messageId,
        authorName = authorName,
        titleLink = titleLink,
        authorLink = authorLink,
        thumbUrl = thumbUrl,
        imageUrl = imageUrl,
        assetUrl = assetUrl,
        ogUrl = ogUrl,
        mimeType = mimeType,
        fileSize = fileSize,
        title = title,
        text = text,
        type = type,
        image = image,
        url = url,
        name = name,
        fallback = fallback,
        uploadFilePath = upload?.absolutePath,
        uploadState = uploadState?.toEntity(),
        originalHeight = originalHeight,
        originalWidth = originalWidth,
        extraData = mutableExtraData,
    )
}

fun Attachment.UploadState.toEntity(): UploadStateEntity {
    val (statusCode, errorMessage) = when (this) {
        Attachment.UploadState.Success -> UPLOAD_STATE_SUCCESS to null
        Attachment.UploadState.Idle -> UPLOAD_STATE_IN_PROGRESS to null
        is Attachment.UploadState.InProgress -> UPLOAD_STATE_IN_PROGRESS to null
        is Attachment.UploadState.Failed -> UPLOAD_STATE_FAILED to (this.error.message)
    }
    return UploadStateEntity(statusCode, errorMessage)
}

fun MessageModerationDetails.toEntity(): ModerationDetailsEntity {
    return ModerationDetailsEntity(
        originalText = originalText,
        action = action.rawValue,
        errorMsg = errorMsg,
    )
}

fun ChannelInfo.toEntity(): ChannelInfoEntity = ChannelInfoEntity(
    cid = cid,
    id = id,
    type = type,
    memberCount = memberCount,
    name = name,
)

fun UploadStateEntity.toModel(uploadFile: File?): Attachment.UploadState = when (this.statusCode) {
    UPLOAD_STATE_SUCCESS -> Attachment.UploadState.Success
    UPLOAD_STATE_IN_PROGRESS -> Attachment.UploadState.InProgress(0, uploadFile?.length() ?: 0)
    UPLOAD_STATE_FAILED -> Attachment.UploadState.Failed(Error.GenericError(message = this.errorMessage ?: ""))
    else -> error("Integer value of $statusCode can't be mapped to UploadState")
}

fun ReplyAttachmentEntity.toModel(): Attachment = Attachment(
    authorName = authorName,
    titleLink = titleLink,
    authorLink = authorLink,
    thumbUrl = thumbUrl,
    imageUrl = imageUrl,
    assetUrl = assetUrl,
    ogUrl = ogUrl,
    mimeType = mimeType,
    fileSize = fileSize,
    title = title,
    text = text,
    type = type,
    image = image,
    url = url,
    name = name,
    fallback = fallback,
    upload = uploadFilePath?.let(::File),
    uploadState = uploadState?.toModel(uploadFilePath?.let(::File)),
    originalHeight = originalHeight,
    originalWidth = originalWidth,
    extraData = extraData,
)



fun minOf(vararg dates: Date?): Date? = dates.reduceOrNull { acc, date -> min(acc, date) }

public fun min(dateA: Date?, dateB: Date?): Date? = when (dateA after dateB) {
    true -> dateB
    else -> dateA
}

suspend fun ChannelUserReadEntity.toModel(getUser: suspend (userId: String) -> User): ChannelUserRead =
    ChannelUserRead(getUser(userId), lastReceivedEventDate, unreadMessages, lastRead, lastReadMessageId)

suspend fun MemberEntity.toModel(getUser: suspend (userId: String) -> User): Member = Member(
    user = getUser(userId),
    createdAt = createdAt,
    updatedAt = updatedAt,
    isInvited = isInvited,
    inviteAcceptedAt = inviteAcceptedAt,
    inviteRejectedAt = inviteRejectedAt,
    shadowBanned = shadowBanned,
    banned = banned,
    channelRole = channelRole,
    notificationsMuted = notificationsMuted,
    status = status,
)

val Channel.lastMessage: Message?
    get() = messages.maxByOrNull { it.createdAt ?: it.createdLocallyAt ?: Date(0) }

fun ChannelUserRead.toEntity(): ChannelUserReadEntity =
    ChannelUserReadEntity(getUserId(), lastReceivedEventDate, unreadMessages, lastRead, lastReadMessageId)

fun Member.toEntity(): MemberEntity = MemberEntity(
    userId = getUserId(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    isInvited = isInvited ?: false,
    inviteAcceptedAt = inviteAcceptedAt,
    inviteRejectedAt = inviteRejectedAt,
    shadowBanned = shadowBanned,
    banned = banned,
    channelRole = channelRole,
    notificationsMuted = notificationsMuted,
    status = status,
)

val ChatClient.logic: LogicRegistry
    get() = resolveDependency<StatePlugin, LogicRegistry>()

fun Message.addMyReaction(reaction: Reaction, enforceUnique: Boolean = false): Message {
    return updateReactions {
        when (enforceUnique) {
            true -> clearOwnReactions(reaction.userId)
            false -> this
        }.let {
            it.copy(
                latestReactions = it.latestReactions + reaction,
                ownReactions = it.ownReactions + reaction,
                reactionCounts = it.reactionCounts + (reaction.type to ((reactionCounts[reaction.type] ?: 0) + 1)),
                reactionScores = it.reactionScores +
                        (reaction.type to ((reactionScores[reaction.type] ?: 0) + reaction.score)),
            )
        }
    }
}



fun ReactionData.clearOwnReactions(userId: String): ReactionData {
    val ownReactionsMap = ownReactions.groupBy { it.type }
    return copy(
        latestReactions = latestReactions.filterNot { it.userId == userId },
        reactionCounts = reactionCounts.mapNotNull { (type, count) ->
            when (val ownReaction = ownReactionsMap[type]) {
                null -> type to count
                else -> type to (count - ownReaction.size)
            }.takeUnless { it.second <= 0 }
        }.toMap(),
        reactionScores = reactionScores.mapNotNull { (type, score) ->
            when (val ownReaction = ownReactionsMap[type]) {
                null -> type to score
                else -> type to (score - ownReaction.sumOf { it.score })
            }.takeUnless { it.second <= 0 }
        }.toMap(),
        ownReactions = emptyList(),
    )
}

fun mergeReactions(
    recentReactions: Collection<Reaction>,
    cachedReactions: Collection<Reaction>,
): Collection<Reaction> {
    return (
            cachedReactions.associateBy(Reaction::type) +
                    recentReactions.associateBy(Reaction::type)
            ).values
}

fun Channel.addMembership(currentUserId: String, member: Member): Channel = copy(
    membership = member.takeIf { it.getUserId() == currentUserId } ?: membership,
)

fun Channel.updateReads(newRead: ChannelUserRead): Channel {
    val oldRead = read.firstOrNull { it.user.id == newRead.user.id }
    return copy(
        read = if (oldRead != null) {
            read - oldRead + newRead
        } else {
            read + newRead
        },
    ).syncUnreadCountWithReads()
}

fun Channel.removeMembership(currentUserId: String?): Channel =
    copy(membership = membership.takeUnless { it?.user?.id == currentUserId })

fun Channel.removeMember(memberUserId: String?): Channel = copy(
    members = members.filterNot { it.user.id == memberUserId },
    memberCount = memberCount - (1.takeIf { members.any { it.user.id == memberUserId } } ?: 0),
)

fun Channel.updateMembership(member: Member): Channel = copy(
    membership = member
        .takeIf { it.getUserId() == membership?.getUserId() }
        ?: membership.also {
            StreamLog.w("Chat:ChannelTools") {
                "[updateMembership] rejected; memberUserId(${member.getUserId()}) != " +
                        "membershipUserId(${membership?.getUserId()})"
            }
        },
)

fun Channel.updateMember(member: Member): Channel = copy(
    members = members.map { iterableMember ->
        iterableMember.takeUnless { it.getUserId() == member.getUserId() } ?: member
    },
)

fun Channel.addMember(member: Member): Channel {
    val memberExists = members.any { it.getUserId() == member.getUserId() }
    return copy(
        members = members + listOfNotNull(member.takeUnless { memberExists }),
        memberCount = memberCount + (1.takeUnless { memberExists } ?: 0),

        )
}

fun Channel.updateMembershipBanned(memberUserId: String, banned: Boolean): Channel = copy(
    membership = membership
        ?.takeIf { it.getUserId() == memberUserId }
        ?.copy(banned = banned)
        ?: membership,
)

fun Channel.updateMemberBanned(
    memberUserId: String,
    banned: Boolean,
    shadow: Boolean,
): Channel = copy(
    members = members.map { member ->
        member.takeUnless { it.user.id == memberUserId }
            ?: member.copy(banned = banned, shadowBanned = shadow)
    },
)

fun QueryChannelsState.toMutableState(): QueryChannelsMutableState = this as QueryChannelsMutableState

fun Collection<Channel>.updateUsers(users: Map<String, User>): List<Channel> = map { it.updateUsers(users) }

fun Channel.updateUsers(users: Map<String, User>): Channel {
    return if (users().map(User::id).any(users::containsKey)) {
        copy(
            messages = messages.updateUsers(users),
            members = members.updateUsers(users).toList(),
            watchers = watchers.updateUsers(users),
            createdBy = users[createdBy.id] ?: createdBy,
            pinnedMessages = pinnedMessages.updateUsers(users),
        )
    } else {
        this
    }
}

fun QueryChannelsPaginationRequest.toAnyChannelPaginationRequest(): AnyChannelPaginationRequest {
    val originalRequest = this
    return AnyChannelPaginationRequest().apply {
        this.channelLimit = originalRequest.channelLimit
        this.channelOffset = originalRequest.channelOffset
        this.messageLimit = originalRequest.messageLimit
        this.sort = originalRequest.sort
    }
}

fun Reaction.enrichWithDataBeforeSending(
    currentUser: User,
    isOnline: Boolean,
    enforceUnique: Boolean,
): Reaction = copy(
    user = currentUser,
    userId = currentUser.id,
    syncStatus = if (isOnline) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED,
    enforceUnique = enforceUnique,
)

public fun Message.isDeleted(): Boolean = deletedAt != null

inline fun Message.updateReactions(actions: ReactionData.() -> ReactionData): Message {
    val reactionData = ReactionData(
        reactionCounts,
        reactionScores,
        latestReactions,
        ownReactions,
    ).actions()
    return copy(
        reactionCounts = reactionData.reactionCounts,
        reactionScores = reactionData.reactionScores,
        latestReactions = reactionData.latestReactions,
        ownReactions = reactionData.ownReactions,
    )
}

suspend fun CoroutineScope.launchWithMutex(mutex: Mutex, block: suspend () -> Unit) = launch {
    mutex.withLock { block() }
}

fun ChannelConfig.toEntity(): ChannelConfigEntity = ChannelConfigEntity(
    channelConfigInnerEntity = with(config) {
        ChannelConfigInnerEntity(
            channelType = this@toEntity.type,
            createdAt = createdAt,
            updatedAt = updatedAt,
            name = name,
            isTypingEvents = typingEventsEnabled,
            isReadEvents = readEventsEnabled,
            isConnectEvents = connectEventsEnabled,
            isSearch = searchEnabled,
            isReactionsEnabled = isReactionsEnabled,
            isThreadEnabled = isThreadEnabled,
            isMutes = muteEnabled,
            uploadsEnabled = uploadsEnabled,
            urlEnrichmentEnabled = urlEnrichmentEnabled,
            customEventsEnabled = customEventsEnabled,
            pushNotificationsEnabled = pushNotificationsEnabled,
            messageRetention = messageRetention,
            maxMessageLength = maxMessageLength,
            automod = automod,
            automodBehavior = automodBehavior,
            blocklistBehavior = blocklistBehavior,
        )
    },
    commands = config.commands.map { it.toEntity(type) },
)

fun Command.toEntity(channelType: String) = CommandInnerEntity(
    name = name,
    description = description,
    args = args,
    set = set,
    channelType = channelType,
)

fun ChannelConfigEntity.toModel(): ChannelConfig = ChannelConfig(
    channelConfigInnerEntity.channelType,
    with(channelConfigInnerEntity) {
        Config(
            createdAt = createdAt,
            updatedAt = updatedAt,
            name = name,
            typingEventsEnabled = isTypingEvents,
            readEventsEnabled = isReadEvents,
            connectEventsEnabled = isConnectEvents,
            searchEnabled = isSearch,
            isReactionsEnabled = isReactionsEnabled,
            isThreadEnabled = isThreadEnabled,
            muteEnabled = isMutes,
            uploadsEnabled = uploadsEnabled,
            urlEnrichmentEnabled = urlEnrichmentEnabled,
            customEventsEnabled = customEventsEnabled,
            pushNotificationsEnabled = pushNotificationsEnabled,
            messageRetention = messageRetention,
            maxMessageLength = maxMessageLength,
            automod = automod,
            automodBehavior = automodBehavior,
            blocklistBehavior = blocklistBehavior,
            commands = commands.map(CommandInnerEntity::toModel),
        )
    },
)

fun Reaction.updateSyncStatus(result: Result<*>): Reaction {
    return when (result) {
        is Result.Success -> copy(syncStatus = SyncStatus.COMPLETED)
        is Result.Failure -> updateFailedReactionSyncStatus(result.value)
    }
}

fun List<Message>.latestOrNull(): Message? = when (size >= 2) {
    true -> {
        val first = first()
        val last = last()
        when (last.createdAfter(first)) {
            true -> last
            else -> first
        }
    }

    else -> lastOrNull()
}

fun Message.createdAfter(that: Message): Boolean {
    val thisDate = this.createdAt ?: this.createdLocallyAt
    val thatDate = that.createdAt ?: that.createdLocallyAt
    return thisDate after thatDate
}

public infix fun Date?.after(that: Date?): Boolean {
    return when {
        this == null -> false
        that == null -> true
        else -> this.after(that)
    }
}

fun Channel.updateLastMessage(
    receivedEventDate: Date,
    message: Message,
    currentUserId: String,
): Channel {
    val createdAt = message.createdAt ?: message.createdLocallyAt
    checkNotNull(createdAt) { "created at cant be null, be sure to set message.createdAt" }

    val newMessages = (
            messages
                .associateBy { it.id } + (message.id to message)
            )
        .values
        .sortedBy { it.createdAt ?: it.createdLocallyAt }

    val newReads = read.map { read ->
        read.takeUnless { it.user.id == currentUserId }
            ?: read.copy(
                lastReceivedEventDate = receivedEventDate,
                unreadMessages = read.let {
                    val hasNewUnreadMessage = receivedEventDate.after(it.lastReceivedEventDate) &&
                            newMessages.size > messages.size &&
                            newMessages.last().id == message.id &&
                            !message.shadowed
                    if (hasNewUnreadMessage) it.unreadMessages.inc() else it.unreadMessages
                },
            )
    }
    return this.copy(
        lastMessageAt = newMessages
            .filterNot { it.parentId != null && !it.showInChannel }
            .last()
            .let { it.createdAt ?: it.createdLocallyAt },
        messages = newMessages,
        read = newReads,
    ).syncUnreadCountWithReads()
}

val ChatEvent.realType get() = when (this) {
    is ConnectedEvent -> "connection.connected"
    else -> type
}

private fun Reaction.updateFailedReactionSyncStatus(error: Error): Reaction {
    return copy(
        syncStatus = if (error.isPermanent()) {
            SyncStatus.FAILED_PERMANENTLY
        } else {
            SyncStatus.SYNC_NEEDED
        },
    )
}

fun CommandInnerEntity.toModel() = Command(
    name = name,
    description = description,
    args = args,
    set = set,
)

fun Date.diff(otherTime: Long): TimeDuration {
    val diff = abs(time - otherTime)
    return TimeDuration.millis(diff)
}

fun Error.NetworkError.isStatusBadRequest(): Boolean {
    return statusCode == 400
}

public fun Error.NetworkError.isValidationError(): Boolean {
    return serverErrorCode == ChatErrorCode.VALIDATION_ERROR.code
}

data class ReactionData(
    val reactionCounts: Map<String, Int>,
    val reactionScores: Map<String, Int>,
    val latestReactions: List<Reaction>,
    val ownReactions: List<Reaction>,
)

fun Message.removeMyReaction(reaction: Reaction): Message =
    updateReactions {
        val removed = ownReactions.filter { it.type == reaction.type && it.userId == reaction.userId }.toSet()
        copy(
            latestReactions = latestReactions.filterNot { it.type == reaction.type && it.userId == reaction.userId },
            ownReactions = ownReactions - removed,
            reactionCounts = reactionCounts.mapNotNull { (type, count) ->
                when (removed.firstOrNull { it.type == type }) {
                    null -> type to count
                    else -> type to (count - 1)
                }.takeUnless { it.second <= 0 }
            }.toMap(),
            reactionScores = reactionScores.mapNotNull { (type, score) ->
                when (val ownReaction = removed.firstOrNull { it.type == type }) {
                    null -> type to score
                    else -> type to (score - ownReaction.score)
                }.takeUnless { it.second <= 0 }
            }.toMap(),
        )
    }

fun Message.isModerationError(currentUserId: String?): Boolean = isMine(currentUserId) &&
        (isError() && isModerationBounce())

fun Message.isError(): Boolean = type == MessageType.ERROR

fun Message.isModerationBounce(): Boolean = moderationDetails?.action == MessageModerationAction.bounce

fun Message.isMine(currentUser: User?): Boolean = currentUser?.id == user.id


fun Message.isMine(currentUserId: String?): Boolean = currentUserId == user.id

fun validateCidWithResult(cid: String): Result<String> {
    return try {
        Result.Success(validateCid(cid))
    } catch (exception: IllegalArgumentException) {
        Result.Failure(Error.ThrowableError(message = "Cid is invalid: $cid", cause = exception))
    }
}

fun Message.updateMessageOnlineState(isOnline: Boolean): Message {
    return this.copy(
        syncStatus = if (isOnline) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED,
        updatedLocallyAt = Date(),
    )
}

fun Message.updateFailedMessage(error: Error): Message {
    return this.copy(
        syncStatus = if (error.isPermanent()) {
            SyncStatus.FAILED_PERMANENTLY
        } else {
            SyncStatus.SYNC_NEEDED
        },
        updatedLocallyAt = Date(),
    )
}

fun MutableGlobalState.updateCurrentUser(currentUser: User?, receivedUser: SelfUser) {
    val me = when (receivedUser) {
        is SelfUserFull -> receivedUser.me
        is SelfUserPart -> currentUser?.mergePartially(receivedUser.me) ?: receivedUser.me
    }

    setBanned(me.isBanned)
    setMutedUsers(me.mutes)
    setChannelMutes(me.channelMutes)
    setTotalUnreadCount(me.totalUnreadCount)
    setChannelUnreadCount(me.unreadChannels)
}

fun generateChannelIdIfNeeded(channelId: String, memberIds: List<String>): String {
    return channelId.ifBlank {
        memberIds.joinToString(prefix = "!members-")
    }
}

fun Channel.users(): List<User> {
    return members.map(Member::user) +
            read.map(ChannelUserRead::user) +
            createdBy +
            messages.flatMap { it.users() } +
            watchers
}

public fun Message.hasPendingAttachments(): Boolean =
    attachments.any {
        it.uploadState is Attachment.UploadState.InProgress ||
                it.uploadState is Attachment.UploadState.Idle
    }

fun Message.isReply(): Boolean = replyTo != null

fun Message.enrichWithCid(newCid: String): Message = copy(
    replyTo = replyTo?.enrichWithCid(newCid),
    cid = newCid,
)

public fun User.mergePartially(that: User): User = this.copy(
    role = that.role,
    createdAt = that.createdAt,
    updatedAt = that.updatedAt,
    lastActive = that.lastActive,
    banned = that.banned,
    name = that.name,
    image = that.image,
    privacySettings = that.privacySettings,
    extraData = that.extraData,
)

fun <T : Any> threadLocal(value: () -> T): ReadOnlyProperty<Any?, T> {
    return ThreadLocalDelegate(value)
}

inline fun SharedPreferences.getNonNullString(key: String, defaultValue: String): String {
    return getString(key, defaultValue)!!
}

@Throws(IllegalArgumentException::class)
public fun Pair<String, String>.toCid(): String {
    val cid = "$first:$second"
    validateCid(cid)
    return cid
}

fun validateCid(cid: String): String = cid.apply {
    require(cid.isNotEmpty()) { "cid can not be empty" }
    require(cid.isNotBlank()) { "cid can not be blank" }
    require(Pattern.compile("^([a-zA-z0-9]|!|-)+:([a-zA-z0-9]|!|-)+$").matcher(cid).matches()) {
        "cid needs to be in the format channelType:channelId. For example, messaging:123"
    }
}

public fun PushDevice.toDevice(): Device =
    Device(
        token = token,
        pushProvider = pushProvider.toDevicePushProvider(),
        providerName = providerName,
    )

fun PushProvider.toDevicePushProvider(): DevicePushProvider = when (this) {
    PushProvider.FIREBASE -> DevicePushProvider.FIREBASE
    PushProvider.HUAWEI -> DevicePushProvider.HUAWEI
    PushProvider.XIAOMI -> DevicePushProvider.XIAOMI
    PushProvider.UNKNOWN -> DevicePushProvider.UNKNOWN
}

fun UploadFileResponse.toUploadedFile() =
    UploadedFile(
        file = this.file,
        thumbUrl = this.thumb_url,
    )

public fun Message.populateMentions(channel: Channel): Message {
    if ('@' !in text) {
        return this
    }
    val text = text.lowercase()
    val mentions = mentionedUsersIds.toMutableSet() + channel.members.mapNotNullTo(mutableListOf()) { member ->
        if (text.contains("@${member.user.name.lowercase()}")) {
            member.user.id
        } else {
            null
        }
    }
    return copy(mentionedUsersIds = mentions.toList())
}

val Attachment.uploadId: String?
    get() = extraData["uploadId"] as String?

fun ChatEvent.enrichIfNeeded(): ChatEvent = when (this) {
    is NewMessageEvent -> copy(message = message.enrichWithCid(cid))
    is MessageDeletedEvent -> copy(message = message.enrichWithCid(cid))
    is MessageUpdatedEvent -> copy(message = message.enrichWithCid(cid))
    is ReactionNewEvent -> copy(message = message.enrichWithCid(cid))
    is ReactionUpdateEvent -> copy(message = message.enrichWithCid(cid))
    is ReactionDeletedEvent -> copy(message = message.enrichWithCid(cid))
    is ChannelUpdatedEvent -> copy(message = message?.enrichWithCid(cid))
    is ChannelTruncatedEvent -> copy(message = message?.enrichWithCid(cid))
    is ChannelUpdatedByUserEvent -> copy(message = message?.enrichWithCid(cid))
    is NotificationMessageNewEvent -> copy(message = message.enrichWithCid(cid))
    else -> this
}

@JvmSynthetic
public inline fun <T : Any> Result<T>.stringify(toString: (data: T) -> String): String {
    return when (this) {
        is Result.Success -> toString(value)
        is Result.Failure -> value.toString()
    }
}

internal fun Message.ensureId(currentUser: User?): Message =
    copy(id = id.takeIf { it.isNotBlank() } ?: generateMessageId(currentUser))

private fun generateMessageId(user: User?): String {
    return "${user?.id}-${UUID.randomUUID()}"
}

@Throws(IllegalStateException::class)
public fun String.cidToTypeAndId(): Pair<String, String> {
    check(isNotEmpty()) { "cid can not be empty" }
    check(':' in this) { "cid needs to be in the format channelType:channelId. For example, messaging:123" }
    return checkNotNull(split(":").takeIf { it.size >= 2 }?.let { it.first() to it.last() })
}

internal fun Date.isLaterThanDays(daysInMillis: Long): Boolean {
    val now = Date()
    return now.time - time > daysInMillis
}

internal fun File.getMimeType(): String =
    MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"

internal fun File.getMediaType(): MediaType = getMimeType().toMediaType()

fun Error.NetworkError.Companion.fromChatErrorCode(
    chatErrorCode: ChatErrorCode,
    statusCode: Int = UNKNOWN_STATUS_CODE,
    cause: Throwable? = null,
): Error.NetworkError {
    return Error.NetworkError(
        message = chatErrorCode.description,
        serverErrorCode = chatErrorCode.code,
        statusCode = statusCode,
        cause = cause,
    )
}

fun Message.getCreatedAtOrDefault(default: Date): Date {
    return getCreatedAtOrNull() ?: default
}

fun Message.getCreatedAtOrNull(): Date? {
    return createdAt ?: createdLocallyAt
}

val NEVER: Date = Date(0)

fun Collection<Channel>.applyPagination(pagination: AnyChannelPaginationRequest): List<Channel> {
    val logger by taggedLogger("Chat:ChannelSort")

    return asSequence()
        .also { channelSequence ->
            logger.d {
                val ids = channelSequence.joinToString { channel -> channel.id }
                "Sorting channels: $ids"
            }
        }
        .sortedWith(pagination.sort.comparator)
        .also { channelSequence ->
            logger.d {
                val ids = channelSequence.joinToString { channel -> channel.id }
                "Sort for channels result: $ids"
            }
        }
        .drop(pagination.channelOffset)
        .take(pagination.channelLimit)
        .toList()
}

fun QueryChannelRequest.toAnyChannelPaginationRequest(): AnyChannelPaginationRequest {
    val originalRequest = this
    val paginationAndValue = pagination()
    return AnyChannelPaginationRequest().apply {
        this.messageLimit = originalRequest.messagesLimit()
        this.messageFilterDirection = paginationAndValue?.first
        this.messageFilterValue = paginationAndValue?.second ?: ""
        this.memberLimit = originalRequest.membersLimit()
        this.memberOffset = originalRequest.membersOffset()
        this.watcherLimit = originalRequest.watchersLimit()
        this.watcherOffset = originalRequest.watchersOffset()
        this.channelLimit = 1
    }
}

fun MessageReadEvent.toChannelUserRead() = ChannelUserRead(
    user = user,
    lastReceivedEventDate = createdAt,
    lastRead = createdAt,
    unreadMessages = 0,
    lastReadMessageId = null,
)

fun NotificationMarkReadEvent.toChannelUserRead() = ChannelUserRead(
    user = user,
    lastReceivedEventDate = createdAt,
    lastRead = createdAt,
    unreadMessages = 0,
    lastReadMessageId = null,
)

fun NotificationMarkUnreadEvent.toChannelUserRead() = ChannelUserRead(
    user = user,
    lastReceivedEventDate = createdAt,
    lastRead = lastReadMessageAt,
    unreadMessages = unreadMessages,
    lastReadMessageId = this.lastReadMessageId,
)

fun MarkAllReadEvent.toChannelUserRead() = ChannelUserRead(
    user = user,
    lastReceivedEventDate = createdAt,
    lastRead = createdAt,
    unreadMessages = 0,
    lastReadMessageId = null,
)

fun Collection<Message>.updateUsers(users: Map<String, User>): List<Message> = map { it.updateUsers(users) }

fun Message.updateUsers(users: Map<String, User>): Message =
    if (users().map(User::id).any(users::containsKey)) {
        copy(
            user = if (users.containsKey(user.id)) {
                users[user.id] ?: user
            } else {
                user
            },
            latestReactions = latestReactions.updateByUsers(users).toMutableList(),
            replyTo = replyTo?.updateUsers(users),
            mentionedUsers = mentionedUsers.updateUsers(users).toMutableList(),
            threadParticipants = threadParticipants.updateUsers(users).toMutableList(),
            pinnedBy = users[pinnedBy?.id ?: ""] ?: pinnedBy,
        )
    } else {
        this
    }

fun Collection<User>.updateUsers(users: Map<String, User>): List<User> = map { user -> users[user.id] ?: user }

fun Message.users(): List<User> {
    return latestReactions.mapNotNull(Reaction::user) +
            user +
            (replyTo?.users().orEmpty()) +
            mentionedUsers +
            ownReactions.mapNotNull(Reaction::user) +
            threadParticipants +
            (pinnedBy?.let { listOf(it) } ?: emptyList())
}

fun Collection<Reaction>.updateByUsers(userMap: Map<String, User>): Collection<Reaction> =
    if (mapNotNull { it.user?.id }.any(userMap::containsKey)) {
        map { reaction ->
            if (userMap.containsKey(reaction.user?.id ?: reaction.userId)) {
                reaction.copy(user = userMap[reaction.userId] ?: reaction.user)
            } else {
                reaction
            }
        }
    } else {
        this
    }

fun Message.wasCreatedAfter(date: Date?): Boolean {
    return createdAt ?: createdLocallyAt ?: NEVER > date
}

private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_TIMEOUT = 408
private const val HTTP_API_ERROR = 500

fun Error.isPermanent(): Boolean {
    return if (this is Error.NetworkError) {
        val temporaryErrors = listOf(HTTP_TOO_MANY_REQUESTS, HTTP_TIMEOUT, HTTP_API_ERROR)

        when {
            statusCode in temporaryErrors -> false
            cause is UnknownHostException -> false
            else -> true
        }
    } else {
        false
    }
}

fun Collection<Member>.updateUsers(userMap: Map<String, User>): Collection<Member> = map { member ->
    if (userMap.containsKey(member.getUserId())) {
        member.copy(user = userMap[member.getUserId()] ?: member.user)
    } else {
        member
    }
}

package com.example.composechatsample.core

import android.content.SharedPreferences
import android.webkit.MimeTypeMap
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
import com.example.composechatsample.core.models.ChannelConfig
import com.example.composechatsample.core.models.Command
import com.example.composechatsample.core.models.Config
import com.example.composechatsample.core.models.MessageModerationAction
import com.example.composechatsample.core.models.MessageType
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.models.TimeDuration
import com.example.composechatsample.core.models.mapper.syncUnreadCountWithReads
import com.example.composechatsample.core.plugin.MutableGlobalState
import com.example.composechatsample.core.plugin.QueryChannelsMutableState
import com.example.composechatsample.data.ChannelConfigEntity
import com.example.composechatsample.data.ChannelConfigInnerEntity
import com.example.composechatsample.data.CommandInnerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.abs

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

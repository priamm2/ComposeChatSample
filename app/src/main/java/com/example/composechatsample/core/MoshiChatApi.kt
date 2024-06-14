package com.example.composechatsample.core

import com.example.composechatsample.core.api.QueryChannelRequest
import com.example.composechatsample.core.api.QueryChannelsRequest
import com.example.composechatsample.core.api.SearchMessagesRequest
import com.example.composechatsample.core.events.ChatEvent
import com.example.composechatsample.core.models.requests.AcceptInviteRequest
import com.example.composechatsample.core.models.requests.AddMembersRequest
import com.example.composechatsample.core.models.AppSettings
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.BannedUser
import com.example.composechatsample.core.models.BannedUsersSort
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.response.ChannelResponse
import com.example.composechatsample.core.models.Device
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.Flag
import com.example.composechatsample.core.models.GuestUser
import com.example.composechatsample.core.models.requests.HideChannelRequest
import com.example.composechatsample.core.models.requests.InviteMembersRequest
import com.example.composechatsample.core.models.requests.MarkReadRequest
import com.example.composechatsample.core.models.requests.MarkUnreadRequest
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Mute
import com.example.composechatsample.core.models.requests.PinnedMessagesRequest
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.requests.RejectInviteRequest
import com.example.composechatsample.core.models.requests.RemoveMembersRequest
import com.example.composechatsample.core.models.SearchMessagesResult
import com.example.composechatsample.core.models.SendEventRequest
import com.example.composechatsample.core.models.TruncateChannelRequest
import com.example.composechatsample.core.models.UpdateChannelPartialRequest
import com.example.composechatsample.core.models.requests.UpdateChannelRequest
import com.example.composechatsample.core.models.requests.UpdateCooldownRequest
import com.example.composechatsample.core.models.UploadedFile
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.VideoCallInfo
import com.example.composechatsample.core.models.VideoCallToken
import com.example.composechatsample.core.models.dto.ChatEventDto
import com.example.composechatsample.core.models.dto.DeviceDto
import com.example.composechatsample.core.models.dto.DownstreamMemberDto
import com.example.composechatsample.core.models.dto.DownstreamMessageDto
import com.example.composechatsample.core.models.dto.DownstreamReactionDto
import com.example.composechatsample.core.models.dto.DownstreamUserDto
import com.example.composechatsample.core.models.dto.PartialUpdateUserDto
import com.example.composechatsample.core.models.dto.UpstreamUserDto
import com.example.composechatsample.core.models.mapper.syncUnreadCountWithReads
import com.example.composechatsample.core.models.mapper.toDomain
import com.example.composechatsample.core.models.mapper.toDto
import com.example.composechatsample.core.models.mapper.toMap
import com.example.composechatsample.core.models.querysort.QuerySorter
import com.example.composechatsample.core.models.requests.AddDeviceRequest
import com.example.composechatsample.core.models.requests.BanUserRequest
import com.example.composechatsample.core.models.requests.FlagMessageRequest
import com.example.composechatsample.core.models.requests.FlagRequest
import com.example.composechatsample.core.models.requests.FlagUserRequest
import com.example.composechatsample.core.models.requests.GuestUserRequest
import com.example.composechatsample.core.models.requests.MuteChannelRequest
import com.example.composechatsample.core.models.requests.MuteUserRequest
import com.example.composechatsample.core.models.requests.PartialUpdateMessageRequest
import com.example.composechatsample.core.models.requests.PartialUpdateUsersRequest
import com.example.composechatsample.core.models.requests.QueryBannedUsersRequest
import com.example.composechatsample.core.models.requests.ReactionRequest
import com.example.composechatsample.core.models.requests.SendMessageRequest
import com.example.composechatsample.core.models.requests.SyncHistoryRequest
import com.example.composechatsample.core.models.requests.TranslateMessageRequest
import com.example.composechatsample.core.models.requests.UpdateMessageRequest
import com.example.composechatsample.core.models.requests.UpdateUsersRequest
import com.example.composechatsample.core.models.requests.VideoCallCreateRequest
import com.example.composechatsample.core.models.requests.VideoCallTokenRequest
import com.example.composechatsample.core.models.response.AppSettingsResponse
import com.example.composechatsample.core.models.response.BannedUserResponse
import com.example.composechatsample.core.models.response.CreateVideoCallResponse
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import okhttp3.ResponseBody
import java.io.File
import java.util.Date

@Suppress("TooManyFunctions", "LargeClass")
internal class MoshiChatApi
@Suppress("LongParameterList")
constructor(
    private val fileUploader: FileUploader,
    private val userApi: UserApi,
    private val guestApi: GuestApi,
    private val messageApi: MessageApi,
    private val channelApi: ChannelApi,
    private val deviceApi: DeviceApi,
    private val moderationApi: ModerationApi,
    private val generalApi: GeneralApi,
    private val configApi: ConfigApi,
    private val callApi: VideoCallApi,
    private val fileDownloadApi: FileDownloadApi,
    private val ogApi: OpenGraphApi,
    private val coroutineScope: CoroutineScope,
    private val userScope: UserScope,
) : ChatApi {

    private val logger by taggedLogger("Chat:MoshiChatApi")

    private val callPostponeHelper: CallPostponeHelper by lazy {
        CallPostponeHelper(
            awaitConnection = {
                _connectionId.first { id -> id.isNotEmpty() }
            },
            userScope = userScope,
        )
    }

    @Volatile
    private var userId: String = ""
        get() {
            if (field == "") {
                logger.e { "userId accessed before being set. Did you forget to call ChatClient.connectUser()?" }
            }
            return field
        }

    private val _connectionId: MutableStateFlow<String> = MutableStateFlow("")

    private val connectionId: String
        get() {
            if (_connectionId.value == "") {
                logger.e { "connectionId accessed before being set. Did you forget to call ChatClient.connectUser()?" }
            }
            return _connectionId.value
        }

    override fun setConnection(userId: String, connectionId: String) {
        logger.d { "[setConnection] userId: '$userId', connectionId: '$connectionId'" }
        this.userId = userId
        this._connectionId.value = connectionId
    }

    override fun releaseConnection() {
        this._connectionId.value = ""
    }

    override fun appSettings(): Call<AppSettings> {
        return configApi.getAppSettings().map(AppSettingsResponse::toDomain)
    }

    override fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message,
    ): Call<Message> {
        return messageApi.sendMessage(
            channelType = channelType,
            channelId = channelId,
            message = SendMessageRequest(
                message = message.toDto(),
                skip_push = message.skipPushNotification,
                skip_enrich_url = message.skipEnrichUrl,
            ),
        ).map { response -> response.message.toDomain() }
    }

    override fun updateMessage(
        message: Message,
    ): Call<Message> {
        return messageApi.updateMessage(
            messageId = message.id,
            message = UpdateMessageRequest(
                message = message.toDto(),
                skip_enrich_url = message.skipEnrichUrl,
            ),
        ).map { response -> response.message.toDomain() }
    }

    override fun partialUpdateMessage(
        messageId: String,
        set: Map<String, Any>,
        unset: List<String>,
        skipEnrichUrl: Boolean,
    ): Call<Message> {
        return messageApi.partialUpdateMessage(
            messageId = messageId,
            body = PartialUpdateMessageRequest(
                set = set,
                unset = unset,
                skip_enrich_url = skipEnrichUrl,
            ),
        ).map { response -> response.message.toDomain() }
    }

    override fun getMessage(messageId: String): Call<Message> {
        return messageApi.getMessage(
            messageId = messageId,
        ).map { response -> response.message.toDomain() }
    }

    override fun deleteMessage(messageId: String, hard: Boolean): Call<Message> {
        return messageApi.deleteMessage(
            messageId = messageId,
            hard = if (hard) true else null,
        ).map { response -> response.message.toDomain() }
    }

    override fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int,
    ): Call<List<Reaction>> {
        return messageApi.getReactions(
            messageId = messageId,
            offset = offset,
            limit = limit,
        ).map { response -> response.reactions.map(DownstreamReactionDto::toDomain) }
    }

    override fun sendReaction(reaction: Reaction, enforceUnique: Boolean): Call<Reaction> {
        return messageApi.sendReaction(
            messageId = reaction.messageId,
            request = ReactionRequest(
                reaction = reaction.toDto(),
                enforce_unique = enforceUnique,
            ),
        ).map { response -> response.reaction.toDomain() }
    }

    override fun deleteReaction(
        messageId: String,
        reactionType: String,
    ): Call<Message> {
        return messageApi.deleteReaction(
            messageId = messageId,
            reactionType = reactionType,
        ).map { response -> response.message.toDomain() }
    }

    override fun addDevice(device: Device): Call<Unit> {
        return deviceApi.addDevices(
            request = AddDeviceRequest(
                device.token,
                device.pushProvider.key,
                device.providerName,
            ),
        ).toUnitCall()
    }

    override fun deleteDevice(device: Device): Call<Unit> {
        return deviceApi.deleteDevice(deviceId = device.token).toUnitCall()
    }

    override fun getDevices(): Call<List<Device>> {
        return deviceApi.getDevices().map { response -> response.devices.map(DeviceDto::toDomain) }
    }

    override fun muteCurrentUser(): Call<Mute> {
        return muteUser(
            userId = userId,
            timeout = null,
        )
    }

    override fun unmuteCurrentUser(): Call<Unit> {
        return unmuteUser(userId)
    }

    override fun muteUser(
        userId: String,
        timeout: Int?,
    ): Call<Mute> {
        return moderationApi.muteUser(
            body = MuteUserRequest(
                target_id = userId,
                user_id = this.userId,
                timeout = timeout,
            ),
        ).map { response -> response.mute.toDomain() }
    }

    override fun unmuteUser(userId: String): Call<Unit> {
        return moderationApi.unmuteUser(
            body = MuteUserRequest(
                target_id = userId,
                user_id = this.userId,
                timeout = null,
            ),
        ).toUnitCall()
    }

    override fun muteChannel(
        channelType: String,
        channelId: String,
        expiration: Int?,
    ): Call<Unit> {
        return moderationApi.muteChannel(
            body = MuteChannelRequest(
                channel_cid = "$channelType:$channelId",
                expiration = expiration,
            ),
        ).toUnitCall()
    }

    override fun unmuteChannel(
        channelType: String,
        channelId: String,
    ): Call<Unit> {
        return moderationApi.unmuteChannel(
            body = MuteChannelRequest(
                channel_cid = "$channelType:$channelId",
                expiration = null,
            ),
        ).toUnitCall()
    }

    override fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback?,
    ): Call<UploadedFile> {
        return CoroutineCall(coroutineScope) {
            if (callback != null) {
                fileUploader.sendFile(
                    channelType = channelType,
                    channelId = channelId,
                    userId = userId,
                    file = file,
                    callback,
                )
            } else {
                fileUploader.sendFile(
                    channelType = channelType,
                    channelId = channelId,
                    userId = userId,
                    file = file,
                )
            }
        }
    }

    override fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback?,
    ): Call<UploadedFile> {
        return CoroutineCall(coroutineScope) {
            if (callback != null) {
                fileUploader.sendImage(
                    channelType = channelType,
                    channelId = channelId,
                    userId = userId,
                    file = file,
                    callback,
                )
            } else {
                fileUploader.sendImage(
                    channelType = channelType,
                    channelId = channelId,
                    userId = userId,
                    file = file,
                )
            }
        }
    }

    override fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit> {
        return CoroutineCall(coroutineScope) {
            fileUploader.deleteFile(
                channelType = channelType,
                channelId = channelId,
                userId = userId,
                url = url,
            )
            Result.Success(Unit)
        }
    }

    override fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit> {
        return CoroutineCall(coroutineScope) {
            fileUploader.deleteImage(
                channelType = channelType,
                channelId = channelId,
                userId = userId,
                url = url,
            )
            Result.Success(Unit)
        }
    }

    override fun flagUser(
        userId: String,
        reason: String?,
        customData: Map<String, String>,
    ): Call<Flag> =
        flag(
            FlagUserRequest(
                targetUserId = userId,
                reason = reason,
                custom = customData,
            ),
        )

    override fun unflagUser(userId: String): Call<Flag> =
        unflag(mutableMapOf("target_user_id" to userId))

    override fun flagMessage(
        messageId: String,
        reason: String?,
        customData: Map<String, String>,
    ): Call<Flag> =
        flag(
            FlagMessageRequest(
                targetMessageId = messageId,
                reason = reason,
                custom = customData,
            ),
        )

    override fun unflagMessage(messageId: String): Call<Flag> =
        unflag(mutableMapOf("target_message_id" to messageId))

    private fun flag(body: FlagRequest): Call<Flag> {
        return moderationApi.flag(body = body).map { response -> response.flag.toDomain() }
    }

    private fun unflag(body: Map<String, String>): Call<Flag> {
        return moderationApi.unflag(body = body).map { response -> response.flag.toDomain() }
    }

    override fun banUser(
        targetId: String,
        timeout: Int?,
        reason: String?,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<Unit> {
        return moderationApi.banUser(
            body = BanUserRequest(
                target_user_id = targetId,
                timeout = timeout,
                reason = reason,
                type = channelType,
                id = channelId,
                shadow = shadow,
            ),
        ).toUnitCall()
    }

    override fun unbanUser(
        targetId: String,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<Unit> {
        return moderationApi.unbanUser(
            targetUserId = targetId,
            channelId = channelId,
            channelType = channelType,
            shadow = shadow,
        ).toUnitCall()
    }

    override fun queryBannedUsers(
        filter: FilterObject,
        sort: QuerySorter<BannedUsersSort>,
        offset: Int?,
        limit: Int?,
        createdAtAfter: Date?,
        createdAtAfterOrEqual: Date?,
        createdAtBefore: Date?,
        createdAtBeforeOrEqual: Date?,
    ): Call<List<BannedUser>> {
        return moderationApi.queryBannedUsers(
            payload = QueryBannedUsersRequest(
                filter_conditions = filter.toMap(),
                sort = sort.toDto(),
                offset = offset,
                limit = limit,
                created_at_after = createdAtAfter,
                created_at_after_or_equal = createdAtAfterOrEqual,
                created_at_before = createdAtBefore,
                created_at_before_or_equal = createdAtBeforeOrEqual,
            ),
        ).map { response -> response.bans.map(BannedUserResponse::toDomain) }
    }

    override fun enableSlowMode(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int,
    ): Call<Channel> = updateCooldown(
        channelType = channelType,
        channelId = channelId,
        cooldownTimeInSeconds = cooldownTimeInSeconds,
    )

    override fun disableSlowMode(
        channelType: String,
        channelId: String,
    ): Call<Channel> = updateCooldown(
        channelType = channelType,
        channelId = channelId,
        cooldownTimeInSeconds = 0,
    )

    private fun updateCooldown(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int,
    ): Call<Channel> {
        return channelApi.updateCooldown(
            channelType = channelType,
            channelId = channelId,
            body = UpdateCooldownRequest.create(cooldownTimeInSeconds),
        ).map(this::flattenChannel)
    }

    override fun stopWatching(channelType: String, channelId: String): Call<Unit> = postponeCall {
        channelApi.stopWatching(
            channelType = channelType,
            channelId = channelId,
            connectionId = connectionId,
            body = emptyMap(),
        ).toUnitCall()
    }

    override fun getPinnedMessages(
        channelType: String,
        channelId: String,
        limit: Int,
        sort: QuerySorter<Message>,
        pagination: PinnedMessagesPagination,
    ): Call<List<Message>> {
        return channelApi.getPinnedMessages(
            channelType = channelType,
            channelId = channelId,
            payload = PinnedMessagesRequest.create(
                limit = limit,
                sort = sort,
                pagination = pagination,
            ),
        ).map { response -> response.messages.map(DownstreamMessageDto::toDomain) }
    }

    override fun updateChannel(
        channelType: String,
        channelId: String,
        extraData: Map<String, Any>,
        updateMessage: Message?,
    ): Call<Channel> {
        return channelApi.updateChannel(
            channelType = channelType,
            channelId = channelId,
            body = UpdateChannelRequest(extraData, updateMessage?.toDto()),
        ).map(this::flattenChannel)
    }

    override fun updateChannelPartial(
        channelType: String,
        channelId: String,
        set: Map<String, Any>,
        unset: List<String>,
    ): Call<Channel> {
        return channelApi.updateChannelPartial(
            channelType = channelType,
            channelId = channelId,
            body = UpdateChannelPartialRequest(set, unset),
        ).map(this::flattenChannel)
    }

    override fun showChannel(
        channelType: String,
        channelId: String,
    ): Call<Unit> {
        return channelApi.showChannel(
            channelType = channelType,
            channelId = channelId,
            body = emptyMap(),
        ).toUnitCall()
    }

    override fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Call<Unit> {
        return channelApi.hideChannel(
            channelType = channelType,
            channelId = channelId,
            body = HideChannelRequest(clearHistory),
        ).toUnitCall()
    }

    override fun truncateChannel(
        channelType: String,
        channelId: String,
        systemMessage: Message?,
    ): Call<Channel> {
        return channelApi.truncateChannel(
            channelType = channelType,
            channelId = channelId,
            body = TruncateChannelRequest(message = systemMessage?.toDto()),
        ).map(this::flattenChannel)
    }

    override fun rejectInvite(channelType: String, channelId: String): Call<Channel> {
        return channelApi.rejectInvite(
            channelType = channelType,
            channelId = channelId,
            body = RejectInviteRequest(),
        ).map(this::flattenChannel)
    }

    override fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String?,
    ): Call<Channel> {
        return channelApi.acceptInvite(
            channelType = channelType,
            channelId = channelId,
            body = AcceptInviteRequest.create(userId = userId, message = message),
        ).map(this::flattenChannel)
    }

    override fun deleteChannel(channelType: String, channelId: String): Call<Channel> {
        return channelApi.deleteChannel(
            channelType = channelType,
            channelId = channelId,
        ).map(this::flattenChannel)
    }

    override fun markRead(channelType: String, channelId: String, messageId: String): Call<Unit> {
        return channelApi.markRead(
            channelType = channelType,
            channelId = channelId,
            request = MarkReadRequest(messageId),
        ).toUnitCall()
    }

    override fun markUnread(channelType: String, channelId: String, messageId: String): Call<Unit> {
        return channelApi.markUnread(
            channelType = channelType,
            channelId = channelId,
            request = MarkUnreadRequest(messageId),
        ).toUnitCall()
    }

    override fun markAllRead(): Call<Unit> {
        return channelApi.markAllRead().toUnitCall()
    }

    override fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
        systemMessage: Message?,
        hideHistory: Boolean?,
        skipPush: Boolean?,
    ): Call<Channel> {
        return channelApi.addMembers(
            channelType = channelType,
            channelId = channelId,
            body = AddMembersRequest(members, systemMessage?.toDto(), hideHistory, skipPush),
        ).map(this::flattenChannel)
    }

    override fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
        systemMessage: Message?,
        skipPush: Boolean?,
    ): Call<Channel> {
        return channelApi.removeMembers(
            channelType = channelType,
            channelId = channelId,
            body = RemoveMembersRequest(members, systemMessage?.toDto(), skipPush),
        ).map(this::flattenChannel)
    }

    override fun inviteMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
        systemMessage: Message?,
        skipPush: Boolean?,
    ): Call<Channel> {
        return channelApi.inviteMembers(
            channelType = channelType,
            channelId = channelId,
            body = InviteMembersRequest(members, systemMessage?.toDto(), skipPush),
        ).map(this::flattenChannel)
    }

    private fun flattenChannel(response: ChannelResponse): Channel {
        return response.channel.toDomain().let { channel ->
            channel.copy(
                watcherCount = response.watcher_count,
                read = response.read.map { it.toDomain(channel.lastMessageAt ?: it.last_read) },
                members = response.members.map(DownstreamMemberDto::toDomain),
                membership = response.membership?.toDomain(),
                messages = response.messages.map { it.toDomain().enrichWithCid(channel.cid) },
                watchers = response.watchers.map(DownstreamUserDto::toDomain),
                hidden = response.hidden,
                hiddenMessagesBefore = response.hide_messages_before,
            ).syncUnreadCountWithReads()
        }
    }

    override fun getNewerReplies(
        parentId: String,
        limit: Int,
        lastId: String?,
    ): Call<List<Message>> = messageApi.getNewerReplies(
        parentId = parentId,
        limit = limit,
        lastId = lastId,
    ).map { response -> response.messages.map(DownstreamMessageDto::toDomain) }

    override fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        return messageApi.getReplies(
            messageId = messageId,
            limit = limit,
        ).map { response -> response.messages.map(DownstreamMessageDto::toDomain) }
    }

    override fun getRepliesMore(messageId: String, firstId: String, limit: Int): Call<List<Message>> {
        return messageApi.getRepliesMore(
            messageId = messageId,
            limit = limit,
            firstId = firstId,
        ).map { response -> response.messages.map(DownstreamMessageDto::toDomain) }
    }

    override fun sendAction(request: SendActionRequest): Call<Message> {
        return messageApi.sendAction(
            messageId = request.messageId,
            request = SendActionRequest(
                channelId = request.channelId,
                messageId = request.messageId,
                type = request.type,
                formData = request.formData,
            ),
        ).map { response -> response.message.toDomain() }
    }

    override fun updateUsers(users: List<User>): Call<List<User>> {
        val map: Map<String, UpstreamUserDto> = users.associateBy({ it.id }, User::toDto)
        return userApi.updateUsers(
            connectionId = connectionId,
            body = UpdateUsersRequest(map),
        ).map { response ->
            response.users.values.map(DownstreamUserDto::toDomain)
        }
    }

    override fun partialUpdateUser(id: String, set: Map<String, Any>, unset: List<String>): Call<User> {
        return userApi.partialUpdateUsers(
            connectionId = connectionId,
            body = PartialUpdateUsersRequest(
                listOf(PartialUpdateUserDto(id = id, set = set, unset = unset)),
            ),
        ).map { response ->
            response.users[id]!!.toDomain()
        }
    }

    override fun getGuestUser(userId: String, userName: String): Call<GuestUser> {
        return guestApi.getGuestUser(
            body = GuestUserRequest.create(userId, userName),
        ).map { response -> GuestUser(response.user.toDomain(), response.access_token) }
    }

    override fun translate(messageId: String, language: String): Call<Message> {
        return messageApi.translate(
            messageId = messageId,
            request = TranslateMessageRequest(language),
        ).map { response -> response.message.toDomain() }
    }

    override fun og(url: String): Call<Attachment> {
        return ogApi.get(url).map { it.toDomain() }
    }

    override fun searchMessages(request: SearchMessagesRequest): Call<List<Message>> {
        val newRequest = com.example.composechatsample.core.models.requests.SearchMessagesRequest(
            filter_conditions = request.channelFilter.toMap(),
            message_filter_conditions = request.messageFilter.toMap(),
            offset = request.offset,
            limit = request.limit,
            next = request.next,
            sort = request.sort,
        )
        return generalApi.searchMessages(newRequest)
            .map { response ->
                response.results.map { resp ->
                    resp.message.toDomain()
                        .let { message ->
                            (message.cid.takeUnless(CharSequence::isBlank) ?: message.channelInfo?.cid)
                                ?.let(message::enrichWithCid)
                                ?: message
                        }
                }
            }
    }

    override fun searchMessages(
        channelFilter: FilterObject,
        messageFilter: FilterObject,
        offset: Int?,
        limit: Int?,
        next: String?,
        sort: QuerySorter<Message>?,
    ): Call<SearchMessagesResult> {
        val newRequest = com.example.composechatsample.core.models.requests.SearchMessagesRequest(
            filter_conditions = channelFilter.toMap(),
            message_filter_conditions = messageFilter.toMap(),
            offset = offset,
            limit = limit,
            next = next,
            sort = sort?.toDto(),
        )
        return generalApi.searchMessages(newRequest)
            .map { response ->
                val results = response.results

                val messages = results.map { resp ->
                    resp.message.toDomain().let { message ->
                        (message.cid.takeUnless(CharSequence::isBlank) ?: message.channelInfo?.cid)
                            ?.let(message::enrichWithCid)
                            ?: message
                    }
                }
                SearchMessagesResult(
                    messages = messages,
                    next = response.next,
                    previous = response.previous,
                    resultsWarning = response.resultsWarning?.toDomain(),
                )
            }
    }

    override fun queryChannels(query: QueryChannelsRequest): Call<List<Channel>> {
        val request = com.example.composechatsample.core.models.requests.QueryChannelsRequest(
            filter_conditions = query.filter.toMap(),
            offset = query.offset,
            limit = query.limit,
            sort = query.sort,
            message_limit = query.messageLimit,
            member_limit = query.memberLimit,
            state = query.state,
            watch = query.watch,
            presence = query.presence,
        )

        val lazyQueryChannelsCall = {
            channelApi.queryChannels(
                connectionId = connectionId,
                request = request,
            ).map { response -> response.channels.map(this::flattenChannel) }
        }

        val isConnectionRequired = query.watch || query.presence
        return if (connectionId.isBlank() && isConnectionRequired) {
            logger.i { "[queryChannels] postponing because an active connection is required" }
            postponeCall(lazyQueryChannelsCall)
        } else {
            lazyQueryChannelsCall()
        }
    }

    override fun queryChannel(channelType: String, channelId: String, query: QueryChannelRequest): Call<Channel> {
        val request = com.example.composechatsample.core.models.requests.QueryChannelRequest(
            state = query.state,
            watch = query.watch,
            presence = query.presence,
            messages = query.messages,
            watchers = query.watchers,
            members = query.members,
            data = query.data,
        )

        val lazyQueryChannelCall = {
            if (channelId.isEmpty()) {
                channelApi.queryChannel(
                    channelType = channelType,
                    connectionId = connectionId,
                    request = request,
                )
            } else {
                channelApi.queryChannel(
                    channelType = channelType,
                    channelId = channelId,
                    connectionId = connectionId,
                    request = request,
                )
            }.map(::flattenChannel)
        }

        val isConnectionRequired = query.watch || query.presence
        return if (connectionId.isBlank() && isConnectionRequired) {
            logger.i { "[queryChannel] postponing because an active connection is required" }
            postponeCall(lazyQueryChannelCall)
        } else {
            lazyQueryChannelCall()
        }
    }

    override fun queryUsers(queryUsers: QueryUsersRequest): Call<List<User>> {
        val request = com.example.composechatsample.core.models.requests.QueryUsersRequest(
            filter_conditions = queryUsers.filter.toMap(),
            offset = queryUsers.offset,
            limit = queryUsers.limit,
            sort = queryUsers.sort,
            presence = queryUsers.presence,
        )
        val lazyQueryUsersCall = {
            userApi.queryUsers(
                connectionId,
                request,
            ).map { response -> response.users.map(DownstreamUserDto::toDomain) }
        }

        return if (connectionId.isBlank() && queryUsers.presence) {
            postponeCall(lazyQueryUsersCall)
        } else {
            lazyQueryUsersCall()
        }
    }

    override fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ): Call<List<Member>> {
        val request = io.getstream.chat.android.client.api2.model.requests.QueryMembersRequest(
            type = channelType,
            id = channelId,
            filter_conditions = filter.toMap(),
            offset = offset,
            limit = limit,
            sort = sort.toDto(),
            members = members.map(Member::toDto),
        )

        return generalApi.queryMembers(request)
            .map { response -> response.members.map(DownstreamMemberDto::toDomain) }
    }

    override fun createVideoCall(
        channelId: String,
        channelType: String,
        callId: String,
        callType: String,
    ): Call<VideoCallInfo> {
        return callApi.createCall(
            channelId = channelId,
            channelType = channelType,
            request = VideoCallCreateRequest(id = callId, type = callType),
        ).map(CreateVideoCallResponse::toDomain)
    }

    override fun getVideoCallToken(callId: String): Call<VideoCallToken> {
        return callApi.getCallToken(callId, VideoCallTokenRequest(callId)).map(VideoCallTokenResponse::toDomain)
    }

    override fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
    ): Call<ChatEvent> {
        val map = mutableMapOf<Any, Any>("type" to eventType)
        map.putAll(extraData)

        return channelApi.sendEvent(
            channelType = channelType,
            channelId = channelId,
            request = SendEventRequest(map),
        ).map { response -> response.event.toDomain() }
    }

    override fun getSyncHistory(channelIds: List<String>, lastSyncAt: String): Call<List<ChatEvent>> {
        return generalApi.getSyncHistory(
            body = SyncHistoryRequest(channelIds, lastSyncAt),
            connectionId = connectionId,
        ).map { response -> response.events.map(ChatEventDto::toDomain) }
    }

    override fun downloadFile(fileUrl: String): Call<ResponseBody> {
        return fileDownloadApi.downloadFile(fileUrl)
    }

    override fun warmUp() {
        generalApi.warmUp().enqueue()
    }

    private fun <T : Any> postponeCall(call: () -> Call<T>): Call<T> {
        return callPostponeHelper.postponeCall(call)
    }
}
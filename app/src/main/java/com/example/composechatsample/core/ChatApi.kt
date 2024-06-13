package com.example.composechatsample.core

import androidx.annotation.CheckResult
import com.example.composechatsample.core.events.ChatEvent
import com.example.composechatsample.core.models.AppSettings
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.BannedUser
import com.example.composechatsample.core.models.BannedUsersSort
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Device
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.Flag
import com.example.composechatsample.core.models.GuestUser
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Mute
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.UploadedFile
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.VideoCallInfo
import com.example.composechatsample.core.models.VideoCallToken
import com.example.composechatsample.core.models.querysort.QuerySorter
import okhttp3.ResponseBody
import java.io.File
import java.util.Date

internal interface ChatApi {

    fun setConnection(userId: String, connectionId: String)

    fun appSettings(): Call<AppSettings>

    @CheckResult
    fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback? = null,
    ): Call<UploadedFile>

    @CheckResult
    fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback? = null,
    ): Call<UploadedFile>

    @CheckResult
    fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit>

    @CheckResult
    fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit>

    @CheckResult
    fun addDevice(device: Device): Call<Unit>

    @CheckResult
    fun deleteDevice(device: Device): Call<Unit>

    @CheckResult
    fun getDevices(): Call<List<Device>>

    @CheckResult
    fun searchMessages(request: SearchMessagesRequest): Call<List<Message>>

    @CheckResult
    fun searchMessages(
        channelFilter: FilterObject,
        messageFilter: FilterObject,
        offset: Int?,
        limit: Int?,
        next: String?,
        sort: QuerySorter<Message>?,
    ): Call<SearchMessagesResult>

    @CheckResult
    fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int,
    ): Call<List<Message>>

    @CheckResult
    fun getReplies(messageId: String, limit: Int): Call<List<Message>>

    @CheckResult
    fun getNewerReplies(
        parentId: String,
        limit: Int,
        lastId: String?,
    ): Call<List<Message>>

    @CheckResult
    fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int,
    ): Call<List<Reaction>>

    @CheckResult
    fun sendReaction(reaction: Reaction, enforceUnique: Boolean): Call<Reaction>

    @CheckResult
    fun sendReaction(
        messageId: String,
        reactionType: String,
        enforceUnique: Boolean,
    ): Call<Reaction> {
        return sendReaction(
            reaction = Reaction(
                messageId = messageId,
                type = reactionType,
                score = 0,
            ),
            enforceUnique = enforceUnique,
        )
    }

    @CheckResult
    fun deleteReaction(messageId: String, reactionType: String): Call<Message>

    @CheckResult
    fun deleteMessage(messageId: String, hard: Boolean = false): Call<Message>

    @CheckResult
    fun sendAction(request: SendActionRequest): Call<Message>

    @CheckResult
    fun getMessage(messageId: String): Call<Message>

    @CheckResult
    fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message,
    ): Call<Message>

    @CheckResult
    fun muteChannel(
        channelType: String,
        channelId: String,
        expiration: Int?,
    ): Call<Unit>

    @CheckResult
    fun unmuteChannel(
        channelType: String,
        channelId: String,
    ): Call<Unit>

    @CheckResult
    fun updateMessage(
        message: Message,
    ): Call<Message>

    @CheckResult
    fun partialUpdateMessage(
        messageId: String,
        set: Map<String, Any>,
        unset: List<String>,
        skipEnrichUrl: Boolean = false,
    ): Call<Message>

    @CheckResult
    fun stopWatching(
        channelType: String,
        channelId: String,
    ): Call<Unit>

    @CheckResult
    fun getPinnedMessages(
        channelType: String,
        channelId: String,
        limit: Int,
        sort: QuerySorter<Message>,
        pagination: PinnedMessagesPagination,
    ): Call<List<Message>>

    @CheckResult
    fun queryChannels(query: QueryChannelsRequest): Call<List<Channel>>

    @CheckResult
    fun updateUsers(users: List<User>): Call<List<User>>

    @CheckResult
    fun partialUpdateUser(
        id: String,
        set: Map<String, Any>,
        unset: List<String>,
    ): Call<User>

    fun queryChannel(
        channelType: String,
        channelId: String = "",
        query: QueryChannelRequest,
    ): Call<Channel>

    @CheckResult
    fun updateChannel(
        channelType: String,
        channelId: String,
        extraData: Map<String, Any>,
        updateMessage: Message?,
    ): Call<Channel>

    @CheckResult
    fun updateChannelPartial(
        channelType: String,
        channelId: String,
        set: Map<String, Any>,
        unset: List<String>,
    ): Call<Channel>

    @CheckResult
    fun enableSlowMode(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int,
    ): Call<Channel>

    @CheckResult
    fun disableSlowMode(
        channelType: String,
        channelId: String,
    ): Call<Channel>

    @CheckResult
    fun markRead(
        channelType: String,
        channelId: String,
        messageId: String = "",
    ): Call<Unit>

    @CheckResult
    fun markUnread(
        channelType: String,
        channelId: String,
        messageId: String,
    ): Call<Unit>

    @CheckResult
    fun showChannel(channelType: String, channelId: String): Call<Unit>

    @CheckResult
    fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Call<Unit>

    @CheckResult
    fun truncateChannel(
        channelType: String,
        channelId: String,
        systemMessage: Message?,
    ): Call<Channel>

    @CheckResult
    fun rejectInvite(channelType: String, channelId: String): Call<Channel>

    @CheckResult
    fun muteCurrentUser(): Call<Mute>

    @CheckResult
    fun unmuteCurrentUser(): Call<Unit>

    @CheckResult
    fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String?,
    ): Call<Channel>

    @CheckResult
    fun deleteChannel(channelType: String, channelId: String): Call<Channel>

    @CheckResult
    fun markAllRead(): Call<Unit>

    @CheckResult
    fun getGuestUser(userId: String, userName: String): Call<GuestUser>

    @CheckResult
    fun queryUsers(queryUsers: QueryUsersRequest): Call<List<User>>

    @CheckResult
    fun addMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
        systemMessage: Message?,
        hideHistory: Boolean?,
        skipPush: Boolean?,
    ): Call<Channel>

    @CheckResult
    fun removeMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
        systemMessage: Message?,
        skipPush: Boolean?,
    ): Call<Channel>

    @CheckResult
    fun inviteMembers(
        channelType: String,
        channelId: String,
        members: List<String>,
        systemMessage: Message?,
        skipPush: Boolean?,
    ): Call<Channel>

    @CheckResult
    fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ): Call<List<Member>>

    @CheckResult
    fun muteUser(
        userId: String,
        timeout: Int?,
    ): Call<Mute>

    @CheckResult
    fun unmuteUser(
        userId: String,
    ): Call<Unit>

    @CheckResult
    fun flagUser(
        userId: String,
        reason: String?,
        customData: Map<String, String>,
    ): Call<Flag>

    @CheckResult
    fun unflagUser(userId: String): Call<Flag>

    @CheckResult
    fun flagMessage(
        messageId: String,
        reason: String?,
        customData: Map<String, String>,
    ): Call<Flag>

    @CheckResult
    fun unflagMessage(messageId: String): Call<Flag>

    @CheckResult
    fun banUser(
        targetId: String,
        timeout: Int?,
        reason: String?,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<Unit>

    @CheckResult
    fun unbanUser(
        targetId: String,
        channelType: String,
        channelId: String,
        shadow: Boolean,
    ): Call<Unit>

    @CheckResult
    fun queryBannedUsers(
        filter: FilterObject,
        sort: QuerySorter<BannedUsersSort>,
        offset: Int?,
        limit: Int?,
        createdAtAfter: Date?,
        createdAtAfterOrEqual: Date?,
        createdAtBefore: Date?,
        createdAtBeforeOrEqual: Date?,
    ): Call<List<BannedUser>>

    @CheckResult
    fun createVideoCall(channelId: String, channelType: String, callId: String, callType: String): Call<VideoCallInfo>

    @CheckResult
    fun getVideoCallToken(callId: String): Call<VideoCallToken>

    @CheckResult
    fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
    ): Call<ChatEvent>

    @CheckResult
    fun translate(messageId: String, language: String): Call<Message>

    @CheckResult
    fun og(url: String): Call<Attachment>

    @CheckResult
    fun getSyncHistory(channelIds: List<String>, lastSyncAt: String): Call<List<ChatEvent>>

    @CheckResult
    fun downloadFile(fileUrl: String): Call<ResponseBody>

    fun warmUp()

    fun releaseConnection()
}
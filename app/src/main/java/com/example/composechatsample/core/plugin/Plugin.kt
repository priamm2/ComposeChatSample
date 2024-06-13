package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.querysort.QuerySorter
import java.util.Date
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.ThreadQueryListener
import com.example.composechatsample.core.events.ChatEvent

public interface Plugin :
    DependencyResolver,
    QueryMembersListener,
    DeleteReactionListener,
    SendReactionListener,
    ThreadQueryListener,
    SendGiphyListener,
    ShuffleGiphyListener,
    DeleteMessageListener,
    SendMessageListener,
    SendAttachmentListener,
    EditMessageListener,
    QueryChannelListener,
    QueryChannelsListener,
    TypingEventListener,
    HideChannelListener,
    MarkAllReadListener,
    ChannelMarkReadListener,
    CreateChannelListener,
    DeleteChannelListener,
    GetMessageListener,
    FetchCurrentUserListener {

    public val errorHandler: ErrorHandler?

    override suspend fun onQueryMembersResult(
        result: Result<List<Member>>,
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ) {
        /* No-Op */
    }

    override suspend fun onDeleteReactionRequest(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
    ) {
        /* No-Op */
    }

    override suspend fun onDeleteReactionResult(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
        result: Result<Message>,
    ) {
        /* No-Op */
    }

    override fun onDeleteReactionPrecondition(currentUser: User?): Result<Unit> = Result.Success(Unit)

    override suspend fun onSendReactionRequest(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    ) {
        /* No-Op */
    }

    override suspend fun onSendReactionResult(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
        result: Result<Reaction>,
    ) {
        /* No-Op */
    }

    override suspend fun onSendReactionPrecondition(
        currentUser: User?,
        reaction: Reaction,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onGetRepliesRequest(
        parentId: String,
        limit: Int,
    ) {
        /* No-Op */
    }

    override suspend fun onGetRepliesResult(
        result: Result<List<Message>>,
        parentId: String,
        limit: Int,
    ) {
        /* No-Op */
    }

    override suspend fun onGetNewerRepliesRequest(
        parentId: String,
        limit: Int,
        lastId: String?,
    ) {
        /* No-Op */
    }

    override suspend fun onGetRepliesMoreRequest(
        parentId: String,
        firstId: String,
        limit: Int,
    ) {
        /* No-Op */
    }

    override suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        parentId: String,
        firstId: String,
        limit: Int,
    ) {
        /* No-Op */
    }

    override suspend fun onGetNewerRepliesResult(
        result: Result<List<Message>>,
        parentId: String,
        limit: Int,
        lastId: String?,
    ) {
        /* No-Op */
    }

    override fun onGiphySendResult(cid: String, result: Result<Message>) {
        /* No-Op */
    }

    override suspend fun onShuffleGiphyResult(cid: String, result: Result<Message>) {
        /* No-Op */
    }

    override suspend fun onMessageDeletePrecondition(messageId: String): Result<Unit> = Result.Success(Unit)

    override suspend fun onMessageDeleteRequest(messageId: String) {
        /* No-Op */
    }

    override suspend fun onMessageDeleteResult(
        originalMessageId: String,
        result: Result<Message>,
    ) {
        /* No-Op */
    }

    override suspend fun onMessageSendResult(
        result: Result<Message>,
        channelType: String,
        channelId: String,
        message: Message,
    ) {
        /* No-Op */
    }

    override suspend fun onMessageEditRequest(message: Message) {
        /* No-Op */
    }

    override suspend fun onMessageEditResult(originalMessage: Message, result: Result<Message>) {
        /* No-Op */
    }

    override suspend fun onQueryChannelPrecondition(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onQueryChannelRequest(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        /* No-Op */
    }

    override suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        /* No-Op */
    }

    override suspend fun onQueryChannelsPrecondition(request: QueryChannelsRequest): Result<Unit> =
        Result.Success(Unit)

    override suspend fun onQueryChannelsRequest(request: QueryChannelsRequest) {
        /* No-Op */
    }

    override suspend fun onQueryChannelsResult(
        result: Result<List<Channel>>,
        request: QueryChannelsRequest,
    ) {
        /* No-Op */
    }

    override fun onTypingEventPrecondition(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ): Result<Unit> = Result.Success(Unit)

    override fun onTypingEventRequest(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ) {
        /* No-Op */
    }

    override fun onTypingEventResult(
        result: Result<ChatEvent>,
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ) {
        /* No-Op */
    }

    override suspend fun onHideChannelPrecondition(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onHideChannelRequest(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ) {
        /* No-Op */
    }

    override suspend fun onHideChannelResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ) {
        /* No-Op */
    }

    override suspend fun onMarkAllReadRequest() {
        /* No-Op */
    }

    override suspend fun onChannelMarkReadPrecondition(
        channelType: String,
        channelId: String,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onCreateChannelRequest(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        extraData: Map<String, Any>,
        currentUser: User,
    ) {
        /* No-Op */
    }

    override suspend fun onCreateChannelResult(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        result: Result<Channel>,
    ) {
        /* No-Op */
    }

    override fun onCreateChannelPrecondition(
        currentUser: User?,
        channelId: String,
        memberIds: List<String>,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onDeleteChannelRequest(
        currentUser: User?,
        channelType: String,
        channelId: String,
    ) {
        /* No-Op */
    }

    override suspend fun onDeleteChannelResult(
        channelType: String,
        channelId: String,
        result: Result<Channel>,
    ) {
        /* No-Op */
    }

    override suspend fun onDeleteChannelPrecondition(
        currentUser: User?,
        channelType: String,
        channelId: String,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onAttachmentSendRequest(channelType: String, channelId: String, message: Message) {
        /* No-Op */
    }

    public fun onUserSet(user: User)

    public fun onUserDisconnected()

    public override suspend fun onGetMessageResult(
        messageId: String,
        result: Result<Message>,
    ) {
        /* No-Op */
    }

    public override suspend fun onFetchCurrentUserResult(
        result: Result<User>,
    ) {
        /* No-Op */
    }
}
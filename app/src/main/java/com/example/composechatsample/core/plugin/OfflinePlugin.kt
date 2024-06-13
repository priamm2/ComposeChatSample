package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.User
import kotlin.reflect.KClass

internal class OfflinePlugin(
    internal val activeUser: User,
    private val queryChannelListener: QueryChannelListener,
    private val threadQueryListener: ThreadQueryListener,
    private val editMessageListener: EditMessageListener,
    private val hideChannelListener: HideChannelListener,
    private val deleteReactionListener: DeleteReactionListener,
    private val sendReactionListener: SendReactionListener,
    private val deleteMessageListener: DeleteMessageListener,
    private val shuffleGiphyListener: ShuffleGiphyListener,
    private val sendMessageListener: SendMessageListener,
    private val sendAttachmentListener: SendAttachmentListener,
    private val queryMembersListener: QueryMembersListener,
    private val createChannelListener: CreateChannelListener,
    private val deleteChannelListener: DeleteChannelListener,
    private val getMessageListener: GetMessageListener,
    private val fetchCurrentUserListener: FetchCurrentUserListener,
    private val provideDependency: (KClass<*>) -> Any? = { null },
) : Plugin,
    QueryChannelListener by queryChannelListener,
    ThreadQueryListener by threadQueryListener,
    EditMessageListener by editMessageListener,
    HideChannelListener by hideChannelListener,
    DeleteReactionListener by deleteReactionListener,
    SendReactionListener by sendReactionListener,
    DeleteMessageListener by deleteMessageListener,
    ShuffleGiphyListener by shuffleGiphyListener,
    SendMessageListener by sendMessageListener,
    QueryMembersListener by queryMembersListener,
    CreateChannelListener by createChannelListener,
    DeleteChannelListener by deleteChannelListener,
    SendAttachmentListener by sendAttachmentListener,
    GetMessageListener by getMessageListener,
    FetchCurrentUserListener by fetchCurrentUserListener {

    override val errorHandler: ErrorHandler? = null

    override fun onUserSet(user: User) {

    }

    override fun onUserDisconnected() {
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> resolveDependency(klass: KClass<T>): T? = provideDependency(klass) as? T
}
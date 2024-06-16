package com.example.composechatsample.core.state

import com.example.composechatsample.core.errors.ErrorHandler
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.plugin.ChannelMarkReadListener
import com.example.composechatsample.core.plugin.ChannelMarkReadListenerState
import com.example.composechatsample.core.plugin.DeleteChannelListener
import com.example.composechatsample.core.plugin.DeleteMessageListener
import com.example.composechatsample.core.plugin.DeleteReactionListener
import com.example.composechatsample.core.plugin.EditMessageListener
import com.example.composechatsample.core.plugin.EditMessageListenerState
import com.example.composechatsample.core.plugin.ErrorHandlerFactory
import com.example.composechatsample.core.plugin.EventHandler
import com.example.composechatsample.core.plugin.FetchCurrentUserListener
import com.example.composechatsample.core.plugin.FetchCurrentUserListenerState
import com.example.composechatsample.core.plugin.GlobalState
import com.example.composechatsample.core.plugin.HideChannelListener
import com.example.composechatsample.core.plugin.HideChannelListenerState
import com.example.composechatsample.core.plugin.LogicRegistry
import com.example.composechatsample.core.plugin.MarkAllReadListener
import com.example.composechatsample.core.plugin.MutableGlobalState
import com.example.composechatsample.core.plugin.Plugin
import com.example.composechatsample.core.plugin.QueryChannelListener
import com.example.composechatsample.core.plugin.QueryChannelListenerState
import com.example.composechatsample.core.plugin.QueryChannelsListener
import com.example.composechatsample.core.plugin.QueryChannelsListenerState
import com.example.composechatsample.core.plugin.SendAttachmentListener
import com.example.composechatsample.core.plugin.SendGiphyListener
import com.example.composechatsample.core.plugin.SendMessageListener
import com.example.composechatsample.core.plugin.SendReactionListener
import com.example.composechatsample.core.plugin.ShuffleGiphyListener
import com.example.composechatsample.core.plugin.StatePluginConfig
import com.example.composechatsample.core.plugin.SyncHistoryManager
import com.example.composechatsample.core.plugin.SyncManager
import com.example.composechatsample.core.plugin.ThreadQueryListener
import com.example.composechatsample.core.plugin.TypingEventListener
import com.example.composechatsample.core.repository.RepositoryFacade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KClass

class StatePlugin internal constructor(
    private val errorHandlerFactory: ErrorHandlerFactory,
    private val logic: LogicRegistry,
    private val repositoryFacade: RepositoryFacade,
    private val clientState: ClientState,
    private val stateRegistry: StateRegistry,
    private val syncManager: SyncManager,
    private val eventHandler: EventHandler,
    private val globalState: MutableGlobalState,
    private val queryingChannelsFree: MutableStateFlow<Boolean>,
    private val statePluginConfig: StatePluginConfig,
) : Plugin,
    QueryChannelsListener by QueryChannelsListenerState(logic, queryingChannelsFree),
    QueryChannelListener by QueryChannelListenerState(logic),
    ThreadQueryListener by ThreadQueryListenerState(logic, repositoryFacade),
    ChannelMarkReadListener by ChannelMarkReadListenerState(stateRegistry),
    EditMessageListener by EditMessageListenerState(logic, clientState),
    HideChannelListener by HideChannelListenerState(logic),
    MarkAllReadListener by MarkAllReadListenerState(logic, stateRegistry),
    DeleteReactionListener by DeleteReactionListenerState(logic, clientState),
    DeleteChannelListener by DeleteChannelListenerState(logic, clientState),
    SendReactionListener by SendReactionListenerState(logic, clientState),
    DeleteMessageListener by DeleteMessageListenerState(logic, clientState, globalState),
    SendGiphyListener by SendGiphyListenerState(logic),
    ShuffleGiphyListener by ShuffleGiphyListenerState(logic),
    SendMessageListener by SendMessageListenerState(logic),
    TypingEventListener by TypingEventListenerState(stateRegistry),
    SendAttachmentListener by SendAttachmentListenerState(logic),
    FetchCurrentUserListener by FetchCurrentUserListenerState(clientState, globalState) {

    override var errorHandler: ErrorHandler = errorHandlerFactory.create()

    override fun onUserSet(user: User) {
        syncManager.start()
        eventHandler.startListening()
    }

    override fun onUserDisconnected() {
        stateRegistry.clear()
        logic.clear()
        syncManager.stop()
        eventHandler.stopListening()
    }

    @Suppress("UNCHECKED_CAST")

    public override fun <T : Any> resolveDependency(klass: KClass<T>): T? = when (klass) {
        SyncHistoryManager::class -> syncManager as T
        EventHandler::class -> eventHandler as T
        LogicRegistry::class -> logic as T
        StateRegistry::class -> stateRegistry as T
        GlobalState::class -> globalState as T
        StatePluginConfig::class -> statePluginConfig as T
        else -> null
    }
}
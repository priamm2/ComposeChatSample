package com.example.composechatsample.core.state

import android.content.Context
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.DispatcherProvider
import com.example.composechatsample.core.events.ChatEvent
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.plugin.EventHandler
import com.example.composechatsample.core.plugin.EventHandlerSequential
import com.example.composechatsample.core.plugin.LogicRegistry
import com.example.composechatsample.core.plugin.MutableGlobalState
import com.example.composechatsample.core.plugin.Plugin
import com.example.composechatsample.core.plugin.PluginFactory
import com.example.composechatsample.core.plugin.StateErrorHandlerFactory
import com.example.composechatsample.core.plugin.StatePluginConfig
import com.example.composechatsample.core.plugin.SyncManager
import com.example.composechatsample.core.repository.RepositoryFacade
import com.example.composechatsample.log.StreamLog
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.job
import kotlin.reflect.KClass

public class StreamStatePluginFactory(
    private val config: StatePluginConfig,
    private val appContext: Context,
) : PluginFactory {
    private val logger by taggedLogger("Chat:StatePluginFactory")

    override fun <T : Any> resolveDependency(klass: KClass<T>): T? {
        return when (klass) {
            StatePluginConfig::class -> config as T
            else -> null
        }
    }

    override fun get(user: User): Plugin {
        logger.d { "[get] user.id: ${user.id}" }
        return createStatePlugin(user)
    }

    private fun createStatePlugin(user: User): StatePlugin {
        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            StreamLog.e("StreamStatePlugin", throwable) {
                "[uncaughtCoroutineException] throwable: $throwable, context: $context"
            }
        }
        val scope = ChatClient.instance().inheritScope { parentJob ->
            SupervisorJob(parentJob) + DispatcherProvider.IO + exceptionHandler
        }
        return createStatePlugin(user, scope, MutableGlobalState())
    }

    @SuppressWarnings("LongMethod")
    private fun createStatePlugin(
        user: User,
        scope: CoroutineScope,
        mutableGlobalState: MutableGlobalState,
    ): StatePlugin {
        logger.v { "[createStatePlugin] user.id: ${user.id}" }
        val chatClient = ChatClient.instance()
        val repositoryFacade = chatClient.repositoryFacade
        val clientState = chatClient.clientState

        val stateRegistry = StateRegistry(
            clientState.user,
            repositoryFacade.observeLatestUsers(),
            scope.coroutineContext.job,
            scope,
        )

        val isQueryingFree = MutableStateFlow(true)

        val logic = LogicRegistry(
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState,
            userPresence = config.userPresence,
            repos = repositoryFacade,
            client = chatClient,
            coroutineScope = scope,
            queryingChannelsFree = isQueryingFree,
        )

        chatClient.logicRegistry = logic

        val syncManager = SyncManager(
            currentUserId = user.id,
            scope = scope,
            chatClient = chatClient,
            clientState = clientState,
            repos = repositoryFacade,
            logicRegistry = logic,
            stateRegistry = stateRegistry,
            userPresence = config.userPresence,
            syncMaxThreshold = config.syncMaxThreshold,
            now = { System.currentTimeMillis() },
        )

        val eventHandler: EventHandler = createEventHandler(
            user = user,
            scope = scope,
            client = chatClient,
            logicRegistry = logic,
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState,
            repos = repositoryFacade,
            syncedEvents = syncManager.syncedEvents,
            sideEffect = syncManager::awaitSyncing,
        )

        if (config.backgroundSyncEnabled) {
            chatClient.setPushNotificationReceivedListener { channelType, channelId ->
                OfflineSyncFirebaseMessagingHandler().syncMessages(appContext, "$channelType:$channelId")
            }
        }

        val stateErrorHandlerFactory = StateErrorHandlerFactory(
            scope = scope,
            logicRegistry = logic,
            clientState = clientState,
            repositoryFacade = repositoryFacade,
        )

        return StatePlugin(
            errorHandlerFactory = stateErrorHandlerFactory,
            logic = logic,
            repositoryFacade = repositoryFacade,
            clientState = clientState,
            stateRegistry = stateRegistry,
            syncManager = syncManager,
            eventHandler = eventHandler,
            globalState = mutableGlobalState,
            queryingChannelsFree = isQueryingFree,
            statePluginConfig = config,
        )
    }

    @Suppress("LongMethod", "LongParameterList")
    private fun createEventHandler(
        user: User,
        scope: CoroutineScope,
        client: ChatClient,
        logicRegistry: LogicRegistry,
        stateRegistry: StateRegistry,
        clientState: ClientState,
        mutableGlobalState: MutableGlobalState,
        repos: RepositoryFacade,
        sideEffect: suspend () -> Unit,
        syncedEvents: Flow<List<ChatEvent>>,
    ): EventHandler {
        return EventHandlerSequential(
            scope = scope,
            currentUserId = user.id,
            subscribeForEvents = { listener -> client.subscribe(listener) },
            logicRegistry = logicRegistry,
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState,
            repos = repos,
            syncedEvents = syncedEvents,
            sideEffect = sideEffect,
        )
    }
}
package com.example.composechatsample.core

import com.example.composechatsample.core.errors.ChatErrorDetail
import com.example.composechatsample.core.events.ChatEvent
import com.example.composechatsample.core.events.ConnectedEvent
import com.example.composechatsample.core.events.ConnectionErrorEvent
import com.example.composechatsample.core.events.HealthEvent
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.state.NetworkStateProvider
import com.example.composechatsample.core.token.TokenManager
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.EmptyCoroutineContext

internal open class ChatSocket(
    private val apiKey: String,
    private val wssUrl: String,
    private val tokenManager: TokenManager,
    private val socketFactory: SocketFactory,
    private val userScope: UserScope,
    private val lifecycleObserver: StreamLifecycleObserver,
    private val networkStateProvider: NetworkStateProvider,
    private val clientDebugger: ChatClientDebugger? = null,
) {
    private var streamWebSocket: StreamWebSocket? = null
    private val logger by taggedLogger(TAG)
    private var connectionConf: SocketFactory.ConnectionConf? = null
    private val listeners = mutableSetOf<SocketListener>()
    private val chatSocketStateService = ChatSocketStateService()
    private var socketStateObserverJob: Job? = null
    private val healthMonitor = HealthMonitor(
        userScope = userScope,
        checkCallback = { (chatSocketStateService.currentState as? State.Connected)?.event?.let(::sendEvent) },
        reconnectCallback = { chatSocketStateService.onWebSocketEventLost() },
    )
    private val lifecycleHandler = object : LifecycleHandler {
        override suspend fun resume() { chatSocketStateService.onResume() }
        override suspend fun stopped() { chatSocketStateService.onStop() }
    }
    private val networkStateListener = object : NetworkStateProvider.NetworkStateListener {
        override suspend fun onConnected() { chatSocketStateService.onNetworkAvailable() }
        override suspend fun onDisconnected() { chatSocketStateService.onNetworkNotAvailable() }
    }

    @Suppress("ComplexMethod")
    private fun observeSocketStateService(): Job {
        var socketListenerJob: Job? = null

        suspend fun connectUser(connectionConf: SocketFactory.ConnectionConf) {
            logger.d { "[connectUser] connectionConf: $connectionConf" }
            userScope.launch { startObservers() }
            this.connectionConf = connectionConf
            socketListenerJob?.cancel()
            when (networkStateProvider.isConnected()) {
                true -> {
                    streamWebSocket = socketFactory.createSocket(connectionConf).apply {
                        socketListenerJob = listen().onEach {
                            when (it) {
                                is StreamWebSocketEvent.Error -> handleError(it.streamError)
                                is StreamWebSocketEvent.Message -> when (val event = it.chatEvent) {
                                    is ConnectionErrorEvent -> handleError(event.toNetworkError())
                                    else -> handleEvent(event)
                                }
                            }
                        }.launchIn(userScope)
                    }
                }
                false -> chatSocketStateService.onNetworkNotAvailable()
            }
        }

        suspend fun reconnect(connectionConf: SocketFactory.ConnectionConf) {
            logger.d { "[reconnect] connectionConf: $connectionConf" }
            connectUser(connectionConf.asReconnectionConf())
        }

        return userScope.launch {
            chatSocketStateService.observer { state ->
                logger.i { "[onSocketStateChanged] state: $state" }
                when (state) {
                    is State.RestartConnection -> {
                        connectionConf?.let { chatSocketStateService.onReconnect(it, false) } ?: run {
                            logger.e { "[onSocketStateChanged] #reconnect; connectionConf is null" }
                            clientDebugger?.onNonFatalErrorOccurred(
                                tag = TAG,
                                src = "onSocketStateChanged",
                                desc = "Failed to reconnect socket on ${state.reason}",
                                error = Error.GenericError("connectionConf is null"),
                            )
                        }
                    }
                    is State.Connected -> {
                        healthMonitor.ack()
                        callListeners { listener -> listener.onConnected(state.event) }
                    }
                    is State.Connecting -> {
                        callListeners { listener -> listener.onConnecting() }
                        when (state.connectionType) {
                            ChatSocketStateService.ConnectionType.INITIAL_CONNECTION ->
                                connectUser(state.connectionConf)
                            ChatSocketStateService.ConnectionType.AUTOMATIC_RECONNECTION ->
                                reconnect(state.connectionConf.asReconnectionConf())
                            ChatSocketStateService.ConnectionType.FORCE_RECONNECTION ->
                                reconnect(state.connectionConf.asReconnectionConf())
                        }
                    }
                    is State.Disconnected -> {
                        when (state) {
                            is State.Disconnected.DisconnectedByRequest -> {
                                streamWebSocket?.close()
                                healthMonitor.stop()
                                userScope.launch { disposeObservers() }
                            }
                            is State.Disconnected.NetworkDisconnected -> {
                                streamWebSocket?.close()
                                healthMonitor.stop()
                            }
                            is State.Disconnected.Stopped -> {
                                streamWebSocket?.close()
                                healthMonitor.stop()
                                disposeNetworkStateObserver()
                            }
                            is State.Disconnected.DisconnectedPermanently -> {
                                streamWebSocket?.close()
                                healthMonitor.stop()
                                userScope.launch { disposeObservers() }
                            }
                            is State.Disconnected.DisconnectedTemporarily -> {
                                healthMonitor.onDisconnected()
                            }
                            is State.Disconnected.WebSocketEventLost -> {
                                streamWebSocket?.close()
                                connectionConf?.let { chatSocketStateService.onReconnect(it, false) }
                            }
                        }
                        callListeners { listener -> listener.onDisconnected(cause = state.cause) }
                    }
                }
            }
        }
    }

    suspend fun connectUser(user: User, isAnonymous: Boolean) {
        logger.d { "[connectUser] user.id: ${user.id}, isAnonymous: $isAnonymous" }
        socketStateObserverJob?.cancel()
        socketStateObserverJob = observeSocketStateService()
        chatSocketStateService.onConnect(
            when (isAnonymous) {
                true -> SocketFactory.ConnectionConf.AnonymousConnectionConf(wssUrl, apiKey, user)
                false -> SocketFactory.ConnectionConf.UserConnectionConf(wssUrl, apiKey, user)
            },
        )
    }

    suspend fun disconnect() {
        logger.d { "[disconnect] no args" }
        connectionConf = null
        chatSocketStateService.onRequiredDisconnect()
    }

    private suspend fun handleEvent(chatEvent: ChatEvent) {
        when (chatEvent) {
            is ConnectedEvent -> chatSocketStateService.onConnectionEstablished(chatEvent)
            is HealthEvent -> healthMonitor.ack()
            else -> callListeners { listener -> listener.onEvent(chatEvent) }
        }
    }

    private suspend fun startObservers() {
        lifecycleObserver.observe(lifecycleHandler)
        networkStateProvider.subscribe(networkStateListener)
    }

    private suspend fun disposeObservers() {
        lifecycleObserver.dispose(lifecycleHandler)
        disposeNetworkStateObserver()
    }

    private fun disposeNetworkStateObserver() {
        networkStateProvider.unsubscribe(networkStateListener)
    }

    private suspend fun handleError(error: Error) {
        logger.e { "[handleError] error: $error" }
        when (error) {
            is Error.NetworkError -> onChatNetworkError(error)
            else -> callListeners { it.onError(error) }
        }
    }

    private suspend fun onChatNetworkError(error: Error.NetworkError) {
        if (ChatErrorCode.isAuthenticationError(error.serverErrorCode)) {
            tokenManager.expireToken()
        }

        when (error.serverErrorCode) {
            ChatErrorCode.UNDEFINED_TOKEN.code,
            ChatErrorCode.INVALID_TOKEN.code,
            ChatErrorCode.API_KEY_NOT_FOUND.code,
            ChatErrorCode.VALIDATION_ERROR.code,
            -> {
                logger.d {
                    "One unrecoverable error happened. Error: $error. Error code: ${error.serverErrorCode}"
                }
                chatSocketStateService.onUnrecoverableError(error)
            }
            else -> chatSocketStateService.onNetworkError(error)
        }
    }

    fun removeListener(listener: SocketListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    fun addListener(listener: SocketListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }
    internal fun sendEvent(event: ChatEvent): Boolean = streamWebSocket?.send(event) ?: false

    internal fun isConnected(): Boolean = chatSocketStateService.currentState is State.Connected

    internal suspend fun awaitConnection(timeoutInMillis: Long = DEFAULT_CONNECTION_TIMEOUT) {
        awaitState<State.Connected>(timeoutInMillis)
    }

    internal suspend inline fun <reified T : State> awaitState(timeoutInMillis: Long) {
        withTimeout(timeoutInMillis) {
            chatSocketStateService.currentStateFlow.first { it is T }
        }
    }

    internal fun connectionIdOrError(): String = when (val state = chatSocketStateService.currentState) {
        is State.Connected -> state.event.connectionId
        else -> error("This state doesn't contain connectionId")
    }

    suspend fun reconnectUser(user: User, isAnonymous: Boolean, forceReconnection: Boolean) {
        logger.d {
            "[reconnectUser] user.id: ${user.id}, isAnonymous: $isAnonymous, forceReconnection: $forceReconnection"
        }
        chatSocketStateService.onReconnect(
            when (isAnonymous) {
                true -> SocketFactory.ConnectionConf.AnonymousConnectionConf(wssUrl, apiKey, user)
                false -> SocketFactory.ConnectionConf.UserConnectionConf(wssUrl, apiKey, user)
            },
            forceReconnection,
        )
    }

    private fun callListeners(call: (SocketListener) -> Unit) {
        synchronized(listeners) {
            listeners.forEach { listener ->
                val context = if (listener.deliverOnMainThread) {
                    DispatcherProvider.Main
                } else {
                    EmptyCoroutineContext
                }
                userScope.launch(context) { call(listener) }
            }
        }
    }

    private val State.Disconnected.cause
        get() = when (this) {
            is State.Disconnected.DisconnectedByRequest,
            is State.Disconnected.Stopped,
            -> DisconnectCause.ConnectionReleased
            is State.Disconnected.NetworkDisconnected -> DisconnectCause.NetworkNotAvailable
            is State.Disconnected.DisconnectedPermanently -> DisconnectCause.UnrecoverableError(error)
            is State.Disconnected.DisconnectedTemporarily -> DisconnectCause.Error(error)
            is State.Disconnected.WebSocketEventLost -> DisconnectCause.WebSocketNotAvailable
        }

    private fun ConnectionErrorEvent.toNetworkError(): Error.NetworkError {
        return Error.NetworkError(
            message = error.message +
                moreInfoTemplate(error.moreInfo) +
                buildDetailsTemplate(error.details),
            serverErrorCode = error.code,
            statusCode = error.statusCode,
        )
    }

    private fun moreInfoTemplate(moreInfo: String): String {
        return if (moreInfo.isNotBlank()) {
            "\nMore information available at $moreInfo"
        } else {
            ""
        }
    }

    private fun buildDetailsTemplate(details: List<ChatErrorDetail>): String {
        return if (details.isNotEmpty()) {
            "\nError details: $details"
        } else {
            ""
        }
    }

    companion object {
        private const val TAG = "Chat:Socket"
        private const val DEFAULT_CONNECTION_TIMEOUT = 60_000L
    }
}
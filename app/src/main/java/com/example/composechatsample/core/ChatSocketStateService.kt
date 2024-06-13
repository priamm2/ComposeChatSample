package com.example.composechatsample.core

import com.example.composechatsample.core.events.ConnectedEvent
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.flow.StateFlow

internal class ChatSocketStateService(initialState: State = State.Disconnected.Stopped) {
    private val logger by taggedLogger("Chat:SocketState")

    suspend fun observer(onNewState: suspend (State) -> Unit) {
        stateMachine.stateFlow.collect(onNewState)
    }

    suspend fun onReconnect(connectionConf: SocketFactory.ConnectionConf, forceReconnection: Boolean) {
        logger.v {
            "[onReconnect] user.id: '${connectionConf.user.id}', isReconnection: ${connectionConf.isReconnection}"
        }
        stateMachine.sendEvent(
            Event.Connect(
                connectionConf,
                when (forceReconnection) {
                    true -> ConnectionType.FORCE_RECONNECTION
                    false -> ConnectionType.AUTOMATIC_RECONNECTION
                },
            ),
        )
    }

    suspend fun onConnect(connectionConf: SocketFactory.ConnectionConf) {
        logger.v {
            "[onConnect] user.id: '${connectionConf.user.id}', isReconnection: ${connectionConf.isReconnection}"
        }
        stateMachine.sendEvent(Event.Connect(connectionConf, ConnectionType.INITIAL_CONNECTION))
    }

    suspend fun onNetworkNotAvailable() {
        logger.w { "[onNetworkNotAvailable] no args" }
        stateMachine.sendEvent(Event.NetworkNotAvailable)
    }

    suspend fun onConnectionEstablished(connectedEvent: ConnectedEvent) {
        logger.i {
            "[onConnected] user.id: '${connectedEvent.me.id}', connectionId: ${connectedEvent.connectionId}"
        }
        stateMachine.sendEvent(Event.ConnectionEstablished(connectedEvent))
    }

    suspend fun onUnrecoverableError(error: Error.NetworkError) {
        logger.e { "[onUnrecoverableError] error: $error" }
        stateMachine.sendEvent(Event.UnrecoverableError(error))
    }

    suspend fun onNetworkError(error: Error.NetworkError) {
        logger.e { "[onNetworkError] error: $error" }
        stateMachine.sendEvent(Event.NetworkError(error))
    }

    suspend fun onRequiredDisconnect() {
        logger.i { "[onRequiredDisconnect] no args" }
        stateMachine.sendEvent(Event.RequiredDisconnection)
    }

    suspend fun onStop() {
        logger.i { "[onStop] no args" }
        stateMachine.sendEvent(Event.Stop)
    }

    suspend fun onWebSocketEventLost() {
        logger.w { "[onWebSocketEventLost] no args" }
        stateMachine.sendEvent(Event.WebSocketEventLost)
    }

    suspend fun onNetworkAvailable() {
        logger.i { "[onNetworkAvailable] no args" }
        stateMachine.sendEvent(Event.NetworkAvailable)
    }

    suspend fun onResume() {
        logger.v { "[onResume] no args" }
        stateMachine.sendEvent(Event.Resume)
    }

    val currentState: State
        get() = stateMachine.state

    val currentStateFlow: StateFlow<State>
        get() = stateMachine.stateFlow

    private val stateMachine: FiniteStateMachine<State, Event> by lazy {
        FiniteStateMachine {
            initialState(initialState)

            defaultHandler { state, event ->
                logger.e { "Cannot handle event $event while being in inappropriate state $state" }
                state
            }

            state<State.RestartConnection> {
                onEvent<Event.Connect> { State.Connecting(it.connectionConf, it.connectionType) }
                onEvent<Event.ConnectionEstablished> { State.Connected(it.connectedEvent) }
                onEvent<Event.WebSocketEventLost> { State.Disconnected.WebSocketEventLost }
                onEvent<Event.NetworkNotAvailable> { State.Disconnected.NetworkDisconnected }
                onEvent<Event.UnrecoverableError> { State.Disconnected.DisconnectedPermanently(it.error) }
                onEvent<Event.NetworkError> { State.Disconnected.DisconnectedTemporarily(it.error) }
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
                onEvent<Event.Stop> { State.Disconnected.Stopped }
            }

            state<State.Connecting> {
                onEvent<Event.Connect> { State.Connecting(it.connectionConf, it.connectionType) }
                onEvent<Event.ConnectionEstablished> { State.Connected(it.connectedEvent) }
                onEvent<Event.WebSocketEventLost> { State.Disconnected.WebSocketEventLost }
                onEvent<Event.NetworkNotAvailable> { State.Disconnected.NetworkDisconnected }
                onEvent<Event.UnrecoverableError> { State.Disconnected.DisconnectedPermanently(it.error) }
                onEvent<Event.NetworkError> { State.Disconnected.DisconnectedTemporarily(it.error) }
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
                onEvent<Event.Stop> { State.Disconnected.Stopped }
            }

            state<State.Connected> {
                onEvent<Event.ConnectionEstablished> { State.Connected(it.connectedEvent) }
                onEvent<Event.WebSocketEventLost> { State.Disconnected.WebSocketEventLost }
                onEvent<Event.NetworkNotAvailable> { State.Disconnected.NetworkDisconnected }
                onEvent<Event.UnrecoverableError> { State.Disconnected.DisconnectedPermanently(it.error) }
                onEvent<Event.NetworkError> { State.Disconnected.DisconnectedTemporarily(it.error) }
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
                onEvent<Event.Stop> { State.Disconnected.Stopped }
            }

            state<State.Disconnected.Stopped> {
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
                onEvent<Event.Connect> { State.Connecting(it.connectionConf, it.connectionType) }
                onEvent<Event.Resume> { State.RestartConnection(RestartReason.LIFECYCLE_RESUME) }
            }

            state<State.Disconnected.NetworkDisconnected> {
                onEvent<Event.Connect> { State.Connecting(it.connectionConf, it.connectionType) }
                onEvent<Event.ConnectionEstablished> { State.Connected(it.connectedEvent) }
                onEvent<Event.UnrecoverableError> { State.Disconnected.DisconnectedPermanently(it.error) }
                onEvent<Event.NetworkError> { State.Disconnected.DisconnectedTemporarily(it.error) }
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
                onEvent<Event.Stop> { State.Disconnected.Stopped }
                onEvent<Event.NetworkAvailable> { State.RestartConnection(RestartReason.NETWORK_AVAILABLE) }
            }

            state<State.Disconnected.WebSocketEventLost> {
                onEvent<Event.Connect> { State.Connecting(it.connectionConf, it.connectionType) }
                onEvent<Event.ConnectionEstablished> { State.Connected(it.connectedEvent) }
                onEvent<Event.NetworkNotAvailable> { State.Disconnected.NetworkDisconnected }
                onEvent<Event.UnrecoverableError> { State.Disconnected.DisconnectedPermanently(it.error) }
                onEvent<Event.NetworkError> { State.Disconnected.DisconnectedTemporarily(it.error) }
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
                onEvent<Event.Stop> { State.Disconnected.Stopped }
            }

            state<State.Disconnected.DisconnectedByRequest> {
                onEvent<Event.RequiredDisconnection> { currentState }
                onEvent<Event.Connect> {
                    when (it.connectionType) {
                        ConnectionType.INITIAL_CONNECTION -> State.Connecting(it.connectionConf, it.connectionType)
                        ConnectionType.AUTOMATIC_RECONNECTION -> this
                        ConnectionType.FORCE_RECONNECTION -> State.Connecting(it.connectionConf, it.connectionType)
                    }
                }
            }

            state<State.Disconnected.DisconnectedTemporarily> {
                onEvent<Event.Connect> { State.Connecting(it.connectionConf, it.connectionType) }
                onEvent<Event.ConnectionEstablished> { State.Connected(it.connectedEvent) }
                onEvent<Event.NetworkNotAvailable> { State.Disconnected.NetworkDisconnected }
                onEvent<Event.WebSocketEventLost> { State.Disconnected.WebSocketEventLost }
                onEvent<Event.UnrecoverableError> { State.Disconnected.DisconnectedPermanently(it.error) }
                onEvent<Event.NetworkError> { State.Disconnected.DisconnectedTemporarily(it.error) }
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
                onEvent<Event.Stop> { State.Disconnected.Stopped }
            }

            state<State.Disconnected.DisconnectedPermanently> {
                onEvent<Event.Connect> {
                    when (it.connectionType) {
                        ConnectionType.INITIAL_CONNECTION -> State.Connecting(it.connectionConf, it.connectionType)
                        ConnectionType.AUTOMATIC_RECONNECTION -> this
                        ConnectionType.FORCE_RECONNECTION -> State.Connecting(it.connectionConf, it.connectionType)
                    }
                }
                onEvent<Event.RequiredDisconnection> { State.Disconnected.DisconnectedByRequest }
            }
        }
    }

    internal enum class ConnectionType {
        INITIAL_CONNECTION,
        AUTOMATIC_RECONNECTION,
        FORCE_RECONNECTION,
    }

    internal enum class RestartReason {
        LIFECYCLE_RESUME,
        NETWORK_AVAILABLE,
    }

    private sealed class Event {

        data class Connect(
            val connectionConf: SocketFactory.ConnectionConf,
            val connectionType: ConnectionType,
        ) : Event()

        data class ConnectionEstablished(val connectedEvent: ConnectedEvent) : Event()

        object WebSocketEventLost : Event() { override fun toString() = "WebSocketEventLost" }

        object NetworkNotAvailable : Event() { override fun toString() = "NetworkNotAvailable" }

        object NetworkAvailable : Event() { override fun toString() = "NetworkAvailable" }

        data class UnrecoverableError(val error: Error.NetworkError) : Event()

        data class NetworkError(val error: Error.NetworkError) : Event()

        object RequiredDisconnection : Event() { override fun toString() = "RequiredDisconnection" }

        object Stop : Event() { override fun toString() = "Stop" }

        object Resume : Event() { override fun toString() = "Resume" }
    }

    internal sealed class State {

        data class RestartConnection(val reason: RestartReason) : State()

        data class Connecting(
            val connectionConf: SocketFactory.ConnectionConf,
            val connectionType: ConnectionType,
        ) : State()

        data class Connected(val event: ConnectedEvent) : State()

        sealed class Disconnected : State() {

            object Stopped : Disconnected() { override fun toString() = "Disconnected.Stopped" }

            object NetworkDisconnected : Disconnected() { override fun toString() = "Disconnected.Network" }

            object WebSocketEventLost : Disconnected() { override fun toString() = "Disconnected.InactiveWS" }

            object DisconnectedByRequest : Disconnected() { override fun toString() = "Disconnected.ByRequest" }

            data class DisconnectedTemporarily(val error: Error.NetworkError) : Disconnected()

            data class DisconnectedPermanently(val error: Error.NetworkError) : Disconnected()
        }
    }
}
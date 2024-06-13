package com.example.composechatsample.core.state

import com.example.composechatsample.core.models.ConnectionState
import com.example.composechatsample.core.models.InitializationState
import com.example.composechatsample.core.models.User
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class MutableClientState(private val networkStateProvider: NetworkStateProvider) : ClientState {

    private val logger by taggedLogger("Chat:ClientState")

    private val _initializationState = MutableStateFlow(InitializationState.NOT_INITIALIZED)
    private val _connectionState: MutableStateFlow<ConnectionState> = MutableStateFlow(ConnectionState.Offline)
    private var _user: MutableStateFlow<User?> = MutableStateFlow(null)

    override val user: StateFlow<User?>
        get() = _user

    override val isOnline: Boolean
        get() = _connectionState.value is ConnectionState.Connected

    override val isOffline: Boolean
        get() = _connectionState.value == ConnectionState.Offline

    override val isConnecting: Boolean
        get() = _connectionState.value == ConnectionState.Connecting

    override val initializationState: StateFlow<InitializationState>
        get() = _initializationState

    override val connectionState: StateFlow<ConnectionState> = _connectionState

    override val isNetworkAvailable: Boolean
        get() = networkStateProvider.isConnected()

    fun clearState() {
        logger.d { "[clearState] no args" }
        _initializationState.value = InitializationState.NOT_INITIALIZED
        _connectionState.value = ConnectionState.Offline
        _user.value = null
    }

    fun setConnectionState(connectionState: ConnectionState) {
        logger.d { "[setConnectionState] state: $connectionState" }
        _connectionState.value = connectionState
    }

    fun setInitializationState(state: InitializationState) {
        _initializationState.value = state
    }

    fun setUser(user: User) {
        _user.value = user
    }
}
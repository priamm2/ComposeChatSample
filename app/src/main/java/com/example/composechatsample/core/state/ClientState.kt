package com.example.composechatsample.core.state;

import com.example.composechatsample.core.models.ConnectionState
import com.example.composechatsample.core.models.InitializationState
import com.example.composechatsample.core.models.User
import kotlinx.coroutines.flow.StateFlow

public interface ClientState {

    public val initializationState: StateFlow<InitializationState>
    public val user: StateFlow<User?>
    public val connectionState: StateFlow<ConnectionState>

    public val isOnline: Boolean
    public val isOffline: Boolean
    public val isConnecting: Boolean

    public val isNetworkAvailable: Boolean
}
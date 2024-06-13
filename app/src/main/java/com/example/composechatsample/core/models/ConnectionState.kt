package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable

@Immutable
public sealed class ConnectionState {

    @Immutable
    public data object Connected : ConnectionState() { override fun toString(): String = "Connected" }

    @Immutable
    public data object Connecting : ConnectionState() { override fun toString(): String = "Connecting" }

    @Immutable
    public data object Offline : ConnectionState() { override fun toString(): String = "Offline" }
}

package com.example.composechatsample.core

public sealed class DisconnectCause {

    public object NetworkNotAvailable : DisconnectCause() { override fun toString(): String = "NetworkNotAvailable" }

    public object WebSocketNotAvailable : DisconnectCause() {
        override fun toString(): String = "WebSocketNotAvailable"
    }

    public data class Error(public val error: com.example.composechatsample.core.Error.NetworkError?) : DisconnectCause()

    public data class UnrecoverableError(public val error: com.example.composechatsample.core.Error.NetworkError?) : DisconnectCause()

    public object ConnectionReleased : DisconnectCause() { override fun toString(): String = "ConnectionReleased" }
}
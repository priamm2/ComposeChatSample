package com.example.composechatsample.core

public sealed class VersionPrefixHeader {
    public abstract val prefix: String

    public data object Default : VersionPrefixHeader() {
        override val prefix: String = "stream-chat-android-"
    }

    public data object UiComponents : VersionPrefixHeader() {
        override val prefix: String = "stream-chat-android-ui-components-"
    }

    public data object Compose : VersionPrefixHeader() {
        override val prefix: String = "stream-chat-android-compose-"
    }
}
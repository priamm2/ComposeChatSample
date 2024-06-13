package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable

@Immutable
public sealed class MessagesState {

    @Immutable
    public data object NoQueryActive : MessagesState() {
        override fun toString(): String = "NoQueryActive"
    }

    @Immutable
    public data object Loading : MessagesState() {
        override fun toString(): String = "Loading"
    }

    @Immutable
    public data object OfflineNoResults : MessagesState() {
        override fun toString(): String = "OfflineNoResults"
    }

    @Immutable
    public data class Result(val messages: List<Message>) : MessagesState()
}

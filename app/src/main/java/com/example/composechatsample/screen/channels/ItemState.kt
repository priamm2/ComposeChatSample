package com.example.composechatsample.screen.channels

import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Message

public sealed class ItemState {
    public abstract val key: String

    public data class ChannelItemState(
        val channel: Channel,
        val isMuted: Boolean = false,
    ) : ItemState() {
        override val key: String = channel.cid
    }

    public data class SearchResultItemState(
        val message: Message,
    ) : ItemState() {
        override val key: String = message.id
    }
}
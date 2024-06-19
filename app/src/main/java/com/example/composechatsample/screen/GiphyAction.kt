package com.example.composechatsample.screen

import com.example.composechatsample.core.models.Message

public sealed class GiphyAction {
    public abstract val message: Message
}

public data class SendGiphy(override val message: Message) : GiphyAction()

public data class ShuffleGiphy(override val message: Message) : GiphyAction()

public data class CancelGiphy(override val message: Message) : GiphyAction()
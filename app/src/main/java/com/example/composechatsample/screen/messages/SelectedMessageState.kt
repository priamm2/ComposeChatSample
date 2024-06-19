package com.example.composechatsample.screen.messages

import com.example.composechatsample.core.models.Message

public sealed class SelectedMessageState {
    public abstract val message: Message
    public abstract val ownCapabilities: Set<String>
}

public data class SelectedMessageOptionsState(
    override val message: Message,
    override val ownCapabilities: Set<String>,
) : SelectedMessageState()

public data class SelectedMessageReactionsState(
    override val message: Message,
    override val ownCapabilities: Set<String>,
) : SelectedMessageState()

public data class SelectedMessageReactionsPickerState(
    override val message: Message,
    override val ownCapabilities: Set<String>,
) : SelectedMessageState()

public data class SelectedMessageFailedModerationState(
    override val message: Message,
    override val ownCapabilities: Set<String>,
) : SelectedMessageState()
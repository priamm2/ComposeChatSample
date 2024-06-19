package com.example.composechatsample.screen.channels

import com.example.composechatsample.core.models.Channel

public sealed class ChannelAction {
    public abstract val channel: Channel
}

public data class ViewInfo(override val channel: Channel) : ChannelAction()

public data class LeaveGroup(override val channel: Channel) : ChannelAction()

public data class MuteChannel(override val channel: Channel) : ChannelAction()

public data class UnmuteChannel(override val channel: Channel) : ChannelAction()

public data class DeleteConversation(override val channel: Channel) : ChannelAction()

public object Cancel : ChannelAction() {
    override val channel: Channel = Channel()
    override fun toString(): String = "Cancel"
}
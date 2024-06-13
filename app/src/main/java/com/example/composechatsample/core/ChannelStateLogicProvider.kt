package com.example.composechatsample.core

public interface ChannelStateLogicProvider {

    public fun channelStateLogic(channelType: String, channelId: String): ChannelMessagesUpdateLogic
}
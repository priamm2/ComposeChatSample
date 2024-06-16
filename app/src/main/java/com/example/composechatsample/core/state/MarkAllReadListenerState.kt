package com.example.composechatsample.core.state

import com.example.composechatsample.core.cidToTypeAndId
import com.example.composechatsample.core.plugin.LogicRegistry
import com.example.composechatsample.core.plugin.MarkAllReadListener

class MarkAllReadListenerState(
    private val logic: LogicRegistry,
    private val state: StateRegistry,
) : MarkAllReadListener {


    override suspend fun onMarkAllReadRequest() {
        logic.getActiveChannelsLogic().map { channel ->
            val (channelType, channelId) = channel.cid.cidToTypeAndId()
            state.markChannelAsRead(channelType = channelType, channelId = channelId)
        }
    }
}
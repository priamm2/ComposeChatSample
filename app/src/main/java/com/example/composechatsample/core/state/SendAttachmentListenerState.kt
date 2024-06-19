package com.example.composechatsample.core.state

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.plugin.LogicRegistry
import com.example.composechatsample.core.plugin.SendAttachmentListener

internal class SendAttachmentListenerState(private val logic: LogicRegistry) :
    SendAttachmentListener {

    override suspend fun onAttachmentSendRequest(channelType: String, channelId: String, message: Message) {
        val channel = logic.channel(channelType, channelId)

        channel.upsertMessage(message)
        logic.threadFromMessage(message)?.upsertMessage(message)
        logic.getActiveQueryChannelsLogic().forEach { query -> query.refreshChannelState(channel.cid) }
    }
}
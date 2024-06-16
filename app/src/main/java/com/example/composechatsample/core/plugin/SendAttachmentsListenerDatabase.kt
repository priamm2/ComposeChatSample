package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.repository.ChannelRepository
import com.example.composechatsample.core.repository.MessageRepository

internal class SendAttachmentsListenerDatabase(
    private val messageRepository: MessageRepository,
    private val channelRepository: ChannelRepository,
) : SendAttachmentListener {

    override suspend fun onAttachmentSendRequest(channelType: String, channelId: String, message: Message) {
        messageRepository.insertMessage(message)
        channelRepository.updateLastMessageForChannel(message.cid, message)
    }
}
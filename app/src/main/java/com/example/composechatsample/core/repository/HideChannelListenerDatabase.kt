package com.example.composechatsample.core.repository

import com.example.composechatsample.core.plugin.HideChannelListener
import com.example.composechatsample.core.toCid
import java.util.Date
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.validateCidWithResult

internal class HideChannelListenerDatabase(
    private val channelRepository: ChannelRepository,
    private val messageRepository: MessageRepository,
) : HideChannelListener {


    override suspend fun onHideChannelPrecondition(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Result<Unit> = validateCidWithResult(Pair(channelType, channelId).toCid()).toUnitResult()

    override suspend fun onHideChannelRequest(channelType: String, channelId: String, clearHistory: Boolean) {

    }

    override suspend fun onHideChannelResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ) {
        if (result is Result.Success) {
            val cid = Pair(channelType, channelId).toCid()

            if (clearHistory) {
                val now = Date()
                channelRepository.setHiddenForChannel(cid, true, now)
                messageRepository.deleteChannelMessagesBefore(cid, now)
            } else {
                channelRepository.setHiddenForChannel(cid, true)
            }
        }
    }
}
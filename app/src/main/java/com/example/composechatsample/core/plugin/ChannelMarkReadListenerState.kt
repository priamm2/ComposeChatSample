package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.Error
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.state.StateRegistry

internal class ChannelMarkReadListenerState(private val state: StateRegistry) : ChannelMarkReadListener {

    override suspend fun onChannelMarkReadPrecondition(channelType: String, channelId: String): Result<Unit> {
        val shouldMarkRead = state.markChannelAsRead(
            channelType = channelType,
            channelId = channelId,
        )

        return if (shouldMarkRead) {
            Result.Success(Unit)
        } else {
            Result.Failure(Error.GenericError("Can not mark channel as read with channel id: $channelId"))
        }
    }
}
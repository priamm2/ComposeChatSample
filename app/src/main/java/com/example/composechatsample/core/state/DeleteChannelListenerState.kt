package com.example.composechatsample.core.state

import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.plugin.DeleteChannelListener
import com.example.composechatsample.core.plugin.LogicRegistry
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.Result

internal class DeleteChannelListenerState(
    private val logic: LogicRegistry,
    private val clientState: ClientState,
) : DeleteChannelListener {


    override suspend fun onDeleteChannelRequest(
        currentUser: User?,
        channelType: String,
        channelId: String,
    ) {
    }


    override suspend fun onDeleteChannelResult(
        channelType: String,
        channelId: String,
        result: Result<Channel>,
    ) {

    }

    override suspend fun onDeleteChannelPrecondition(
        currentUser: User?,
        channelType: String,
        channelId: String,
    ): Result<Unit> {
        return if (currentUser != null) {
            Result.Success(Unit)
        } else {
            Result.Failure(Error.GenericError(message = "Current user is null!"))
        }
    }
}
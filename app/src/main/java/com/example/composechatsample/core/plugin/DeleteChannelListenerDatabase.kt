package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.repository.ChannelRepository
import com.example.composechatsample.core.repository.UserRepository
import com.example.composechatsample.core.state.ClientState
import com.example.composechatsample.core.Result

internal class DeleteChannelListenerDatabase(
    private val clientState: ClientState,
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository,
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
        return Result.Success(Unit)
    }
}
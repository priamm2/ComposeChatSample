package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.Result

public interface DeleteChannelListener {

    public suspend fun onDeleteChannelRequest(
        currentUser: User?,
        channelType: String,
        channelId: String,
    )

    public suspend fun onDeleteChannelResult(
        channelType: String,
        channelId: String,
        result: Result<Channel>,
    )

    public suspend fun onDeleteChannelPrecondition(
        currentUser: User?,
        channelType: String,
        channelId: String,
    ): Result<Unit>
}
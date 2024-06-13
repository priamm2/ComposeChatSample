package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.User

public interface CreateChannelListener {

    public suspend fun onCreateChannelRequest(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        extraData: Map<String, Any>,
        currentUser: User,
    )

    public suspend fun onCreateChannelResult(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        result: Result<Channel>,
    )

    public fun onCreateChannelPrecondition(
        currentUser: User?,
        channelId: String,
        memberIds: List<String>,
    ): Result<Unit>
}
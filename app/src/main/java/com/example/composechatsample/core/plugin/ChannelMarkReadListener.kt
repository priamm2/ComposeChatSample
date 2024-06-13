package com.example.composechatsample.core.plugin

public interface ChannelMarkReadListener {

    public suspend fun onChannelMarkReadPrecondition(
        channelType: String,
        channelId: String,
    ): Result<Unit>
}
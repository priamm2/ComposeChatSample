package com.example.composechatsample.core.plugin
import com.example.composechatsample.core.Result

public interface ChannelMarkReadListener {

    public suspend fun onChannelMarkReadPrecondition(
        channelType: String,
        channelId: String,
    ): Result<Unit>
}
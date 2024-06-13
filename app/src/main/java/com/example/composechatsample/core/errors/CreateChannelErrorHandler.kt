package com.example.composechatsample.core.errors

import com.example.composechatsample.core.Call
import com.example.composechatsample.core.ReturnOnErrorCall
import com.example.composechatsample.core.models.Channel

public interface CreateChannelErrorHandler {

    public fun onCreateChannelError(
        originalCall: Call<Channel>,
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        extraData: Map<String, Any>,
    ): ReturnOnErrorCall<Channel>
}

internal fun Call<Channel>.onCreateChannelError(
    errorHandlers: List<CreateChannelErrorHandler>,
    channelType: String,
    channelId: String,
    memberIds: List<String>,
    extraData: Map<String, Any>,
): Call<Channel> {
    return errorHandlers.fold(this) { createChannelCall, errorHandler ->
        errorHandler.onCreateChannelError(
            originalCall = createChannelCall,
            channelId = channelId,
            channelType = channelType,
            memberIds = memberIds,
            extraData = extraData,
        )
    }
}
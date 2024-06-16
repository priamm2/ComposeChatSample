package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.Result

public interface GetMessageListener {

    public suspend fun onGetMessageResult(
        messageId: String,
        result: Result<Message>,
    )
}
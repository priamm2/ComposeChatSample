package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.Result

public interface DeleteMessageListener {

    public suspend fun onMessageDeletePrecondition(messageId: String): Result<Unit>

    public suspend fun onMessageDeleteRequest(messageId: String)

    public suspend fun onMessageDeleteResult(originalMessageId: String, result: Result<Message>)
}
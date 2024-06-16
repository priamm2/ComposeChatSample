package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.Result

public interface EditMessageListener {

    public suspend fun onMessageEditRequest(message: Message)

    public suspend fun onMessageEditResult(originalMessage: Message, result: Result<Message>)
}
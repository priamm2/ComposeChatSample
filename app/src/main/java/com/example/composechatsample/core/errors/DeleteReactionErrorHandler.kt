package com.example.composechatsample.core.errors

import com.example.composechatsample.core.Call
import com.example.composechatsample.core.ReturnOnErrorCall
import com.example.composechatsample.core.models.Message

public interface DeleteReactionErrorHandler {

    public fun onDeleteReactionError(
        originalCall: Call<Message>,
        cid: String?,
        messageId: String,
    ): ReturnOnErrorCall<Message>
}

internal fun Call<Message>.onMessageError(
    errorHandlers: List<DeleteReactionErrorHandler>,
    cid: String?,
    messageId: String,
): Call<Message> {
    return errorHandlers.fold(this) { messageCall, errorHandler ->
        errorHandler.onDeleteReactionError(messageCall, cid, messageId)
    }
}
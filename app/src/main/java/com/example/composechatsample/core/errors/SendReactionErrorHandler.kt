package com.example.composechatsample.core.errors

import com.example.composechatsample.core.Call
import com.example.composechatsample.core.ReturnOnErrorCall
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.User

public interface SendReactionErrorHandler {

    public fun onSendReactionError(
        originalCall: Call<Reaction>,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    ): ReturnOnErrorCall<Reaction>
}

internal fun Call<Reaction>.onReactionError(
    errorHandlers: List<SendReactionErrorHandler>,
    reaction: Reaction,
    enforceUnique: Boolean,
    currentUser: User,
): Call<Reaction> {
    return errorHandlers.fold(this) { originalCall, errorHandler ->
        errorHandler.onSendReactionError(originalCall, reaction, enforceUnique, currentUser)
    }
}
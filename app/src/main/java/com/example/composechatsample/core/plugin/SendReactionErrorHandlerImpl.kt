package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.Call
import com.example.composechatsample.core.ReturnOnErrorCall
import com.example.composechatsample.core.errors.SendReactionErrorHandler
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.onErrorReturn
import com.example.composechatsample.core.state.ClientState
import kotlinx.coroutines.CoroutineScope
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.enrichWithDataBeforeSending

internal class SendReactionErrorHandlerImpl(
    private val scope: CoroutineScope,
    private val clientState: ClientState,
) : SendReactionErrorHandler {


    override fun onSendReactionError(
        originalCall: Call<Reaction>,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    ): ReturnOnErrorCall<Reaction> {
        return originalCall.onErrorReturn(scope) { originalError ->
            if (clientState.isOnline) {
                Result.Failure(originalError)
            } else {
                Result.Success(
                    reaction.enrichWithDataBeforeSending(
                        currentUser = currentUser,
                        isOnline = clientState.isOnline,
                        enforceUnique = enforceUnique,
                    ),
                )
            }
        }
    }
}
package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.Call
import com.example.composechatsample.core.ReturnOnErrorCall
import com.example.composechatsample.core.cidToTypeAndId
import com.example.composechatsample.core.errors.DeleteReactionErrorHandler
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.onErrorReturn
import com.example.composechatsample.core.state.ClientState
import kotlinx.coroutines.CoroutineScope
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.Result

internal class DeleteReactionErrorHandlerImpl(
    private val scope: CoroutineScope,
    private val logic: LogicRegistry,
    private val clientState: ClientState,
) : DeleteReactionErrorHandler {

    override fun onDeleteReactionError(
        originalCall: Call<Message>,
        cid: String?,
        messageId: String,
    ): ReturnOnErrorCall<Message> {
        return originalCall.onErrorReturn(scope) { originalError ->
            if (cid == null || clientState.isOnline) {
                Result.Failure(originalError)
            } else {
                val (channelType, channelId) = cid.cidToTypeAndId()
                val cachedMessage =
                    logic.channel(channelType = channelType, channelId = channelId).getMessage(messageId)

                if (cachedMessage != null) {
                    Result.Success(cachedMessage)
                } else {
                    Result.Failure(Error.GenericError(message = "Local message was not found."))
                }
            }
        }
    }
}
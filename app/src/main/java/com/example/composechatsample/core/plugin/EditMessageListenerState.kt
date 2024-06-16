package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.Result
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.state.ClientState
import com.example.composechatsample.core.updateFailedMessage
import com.example.composechatsample.core.updateMessageOnlineState

internal class EditMessageListenerState(
    private val logic: LogicRegistry,
    private val clientState: ClientState,
) : EditMessageListener {

    override suspend fun onMessageEditRequest(message: Message) {
        val isOnline = clientState.isNetworkAvailable
        val messagesToEdit = message.updateMessageOnlineState(isOnline)

        logic.channelFromMessage(messagesToEdit)?.stateLogic()?.upsertMessage(messagesToEdit)
        logic.threadFromMessage(messagesToEdit)?.stateLogic()?.upsertMessage(messagesToEdit)
    }

    override suspend fun onMessageEditResult(originalMessage: Message, result: Result<Message>) {
        val parsedMessage = when (result) {
            is Result.Success -> result.value.copy(syncStatus = SyncStatus.COMPLETED)
            is Result.Failure -> originalMessage.updateFailedMessage(result.value)
        }

        logic.channelFromMessage(parsedMessage)?.stateLogic()?.upsertMessage(parsedMessage)
        logic.threadFromMessage(parsedMessage)?.stateLogic()?.upsertMessage(parsedMessage)
    }
}
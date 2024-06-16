package com.example.composechatsample.core.repository

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.plugin.EditMessageListener
import com.example.composechatsample.core.state.ClientState
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.updateFailedMessage
import com.example.composechatsample.core.updateMessageOnlineState
import com.example.composechatsample.core.users

internal class EditMessageListenerDatabase(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
    private val clientState: ClientState,
) : EditMessageListener {


    override suspend fun onMessageEditRequest(message: Message) {
        val isOnline = clientState.isNetworkAvailable
        val messagesToEdit = message.updateMessageOnlineState(isOnline)

        saveMessage(messagesToEdit)
    }


    override suspend fun onMessageEditResult(originalMessage: Message, result: Result<Message>) {
        val parsedMessage = when (result) {
            is Result.Success -> result.value.copy(syncStatus = SyncStatus.COMPLETED)
            is Result.Failure -> originalMessage.updateFailedMessage(result.value)
        }

        saveMessage(parsedMessage)
    }

    private suspend fun saveMessage(message: Message) {
        userRepository.insertUsers(message.users())
        messageRepository.insertMessage(message)
    }
}
package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.repository.MessageRepository
import com.example.composechatsample.core.repository.UserRepository
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.users

internal class ShuffleGiphyListenerDatabase(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
) : ShuffleGiphyListener {


    override suspend fun onShuffleGiphyResult(cid: String, result: Result<Message>) {
        if (result is Result.Success) {
            val processedMessage = result.value.copy(syncStatus = SyncStatus.COMPLETED)
            userRepository.insertUsers(processedMessage.users())
            messageRepository.insertMessage(processedMessage)
        }
    }
}
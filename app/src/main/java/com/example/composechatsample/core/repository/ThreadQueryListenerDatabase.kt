package com.example.composechatsample.core.repository

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.plugin.ThreadQueryListener
import com.example.composechatsample.core.Result

internal class ThreadQueryListenerDatabase(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
) : ThreadQueryListener {

    override suspend fun onGetRepliesRequest(parentId: String, limit: Int) {
        // Nothing to do.
    }

    override suspend fun onGetRepliesResult(result: Result<List<Message>>, parentId: String, limit: Int) {
        onResult(result)
    }

    override suspend fun onGetRepliesMoreRequest(parentId: String, firstId: String, limit: Int) {
        // Nothing to do.
    }

    override suspend fun onGetNewerRepliesRequest(parentId: String, limit: Int, lastId: String?) {
        // Nothing to do.
    }

    override suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        parentId: String,
        firstId: String,
        limit: Int,
    ) {
        onResult(result)
    }

    override suspend fun onGetNewerRepliesResult(
        result: Result<List<Message>>,
        parentId: String,
        limit: Int,
        lastId: String?,
    ) {
        onResult(result)
    }

    private suspend fun onResult(result: Result<List<Message>>) {
        if (result is Result.Success) {
            val messages = result.value

            userRepository.insertUsers(messages.flatMap(Message::users))
            messageRepository.insertMessages(messages)
        }
    }
}
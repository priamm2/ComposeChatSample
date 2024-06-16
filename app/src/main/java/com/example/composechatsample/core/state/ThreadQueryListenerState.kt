package com.example.composechatsample.core.state

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.repository.MessageRepository
import com.example.composechatsample.log.taggedLogger

import com.example.composechatsample.core.Error
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.plugin.LogicRegistry
import com.example.composechatsample.core.plugin.ThreadLogic
import com.example.composechatsample.core.plugin.ThreadQueryListener

internal class ThreadQueryListenerState(
    private val logic: LogicRegistry,
    private val messageRepository: MessageRepository,
) : ThreadQueryListener {

    private val logger by taggedLogger("Chat:ThreadQueryListener")

    override suspend fun onGetRepliesPrecondition(parentId: String): Result<Unit> {
        val loadingMoreMessage = logic.thread(parentId).isLoadingMessages()

        return if (loadingMoreMessage) {
            val errorMsg = "already loading messages for this thread, ignoring the load requests."
            logger.i { errorMsg }
            Result.Failure(Error.GenericError(errorMsg))
        } else {
            Result.Success(Unit)
        }
    }

    override suspend fun onGetRepliesRequest(parentId: String, limit: Int) {
        val threadLogic = logic.thread(parentId)

        threadLogic.setLoading(true)
    }

    override suspend fun onGetRepliesResult(result: Result<List<Message>>, parentId: String, limit: Int) {
        val threadLogic = logic.thread(parentId)
        threadLogic.setLoading(false)
        onResult(threadLogic, result, limit)
    }

    override suspend fun onGetRepliesMoreRequest(parentId: String, firstId: String, limit: Int) {
        logic.thread(parentId).setLoading(true)
    }

    override suspend fun onGetNewerRepliesRequest(parentId: String, limit: Int, lastId: String?) {
        logic.thread(parentId).setLoading(true)
    }

    override suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        parentId: String,
        firstId: String,
        limit: Int,
    ) {
        val threadLogic = logic.thread(parentId)

        threadLogic.setLoading(false)
        onResult(threadLogic, result, limit)
    }

    override suspend fun onGetNewerRepliesResult(
        result: Result<List<Message>>,
        parentId: String,
        limit: Int,
        lastId: String?,
    ) {
        val threadLogic = logic.thread(parentId)
        result.onSuccess { messages ->
            threadLogic.upsertMessages(messages)
            threadLogic.setEndOfNewerMessages(messages.size < limit)
            threadLogic.updateNewestMessageInThread(messages)
        }
        threadLogic.setLoading(false)
    }

    private fun onResult(threadLogic: ThreadLogic?, result: Result<List<Message>>, limit: Int) {
        if (result is Result.Success) {
            val newMessages = result.value
            threadLogic?.run {
                upsertMessages(newMessages)
                setEndOfOlderMessages(newMessages.size < limit)
                updateOldestMessageInThread(newMessages)
            }
        }
    }
}
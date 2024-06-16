package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.repository.RepositoryFacade
import com.example.composechatsample.log.StreamLog
import com.example.composechatsample.core.Result

internal class GetMessageListenerDatabase(
    private val repositoryFacade: RepositoryFacade,
) : GetMessageListener {


    private val logger = StreamLog.getLogger("Chat: GetMessageListenerDatabase")


    override suspend fun onGetMessageResult(
        messageId: String,
        result: Result<Message>,
    ) {
        when (result) {
            is Result.Success -> {
                repositoryFacade.insertMessage(
                    message = result.value,
                )
            }
            is Result.Failure -> {
                val error = result.value
                logger.e {
                    "[onGetMessageResult] Could not insert the message into the database. The API call " +
                        "had failed with: ${error.message}"
                }
            }
        }
    }
}
package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.repository.UserRepository
import com.example.composechatsample.log.taggedLogger
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.onSuccessSuspend


internal class FetchCurrentUserListenerDatabase(
    private val userRepository: UserRepository,
) : FetchCurrentUserListener {

    private val logger by taggedLogger("Chat:FetchCurUserLDB")

    override suspend fun onFetchCurrentUserResult(result: Result<User>) {
        result.onSuccessSuspend {
            logger.d { "[onFetchCurrentUserResult] result: $result" }
            userRepository.insertCurrentUser(it)
        }
    }
}
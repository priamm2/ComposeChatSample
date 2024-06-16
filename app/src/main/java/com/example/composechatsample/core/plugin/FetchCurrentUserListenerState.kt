package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.state.ClientState
import com.example.composechatsample.log.taggedLogger
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.SelfUserFull
import com.example.composechatsample.core.updateCurrentUser

internal class FetchCurrentUserListenerState(
    private val clientState: ClientState,
    private val globalMutableState: MutableGlobalState,
) : FetchCurrentUserListener {

    private val logger by taggedLogger("Chat:FetchCurUserLST")

    override suspend fun onFetchCurrentUserResult(result: Result<User>) {
        if (result.isSuccess) {
            logger.d { "[onFetchCurrentUserResult] result: $result" }
            globalMutableState.updateCurrentUser(
                currentUser = clientState.user.value,
                receivedUser = SelfUserFull(result.getOrThrow()),
            )
        }
    }
}
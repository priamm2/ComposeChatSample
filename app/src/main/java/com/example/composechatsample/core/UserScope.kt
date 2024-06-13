package com.example.composechatsample.core

import com.example.composechatsample.core.models.UserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.plus

internal interface UserScope : CoroutineScope {
    val userId: UserIdentifier
    fun cancelChildren(userId: UserId? = null)
}

internal fun UserScope(clientScope: ClientScope): UserScope = UserScopeImpl(clientScope)

private class UserScopeImpl(
    clientScope: ClientScope,
    userIdentifier: UserIdentifier = UserIdentifier(),
) : UserScope,
    CoroutineScope by (
        clientScope + userIdentifier + UserJob(clientScope.coroutineContext.job) { userIdentifier.value }
        ) {

    override val userId: UserIdentifier
        get() = coroutineContext[UserIdentifier] ?: error("no UserIdentifier found")


    override fun cancelChildren(userId: UserId?) {
        (coroutineContext[Job] as UserJob).cancelChildren(userId)
    }
}
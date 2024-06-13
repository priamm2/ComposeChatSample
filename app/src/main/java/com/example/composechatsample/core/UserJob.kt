@file:OptIn(InternalCoroutinesApi::class)
package com.example.composechatsample.core

import com.example.composechatsample.core.models.UserId
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.ChildHandle
import kotlinx.coroutines.ChildJob
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

internal interface UserJob : CompletableJob {
    fun cancelChildren(userId: UserId? = null)
}

internal fun UserJob(parent: Job? = null, getUserId: () -> UserId?): UserJob = UserJobImpl(
    SupervisorJob(parent),
    getUserId,
)

private class UserJobImpl(
    private val delegate: CompletableJob,
    private val getUserId: () -> UserId?,
) : CompletableJob by delegate, UserJob {

    private val logger by taggedLogger("Chat:UserJob")

    override fun attachChild(child: ChildJob): ChildHandle {
        val userId = getUserId()
        return delegate.attachChild(UserChildJob(userId, child))
    }

    override fun cancelChildren(userId: UserId?) {
        logger.d { "[cancelChildren] userId: '$userId'" }
        for (child in children) {
            if (child is UserChildJob && child.userId != userId && userId != null) {
                logger.v { "[cancelChildren] skip child: $child)" }
                continue
            }
            logger.v { "[cancelChildren] cancel child: $child)" }
            child.cancel()
        }
    }

    override fun <R> fold(initial: R, operation: (R, CoroutineContext.Element) -> R): R {
        return super<UserJob>.fold(initial, operation)
    }

    override fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? {
        return super<UserJob>.get(key)
    }

    override fun minusKey(key: CoroutineContext.Key<*>): CoroutineContext {
        return super<UserJob>.minusKey(key)
    }

    override fun toString(): String = "UserJob(userId=${getUserId()})"
}

private class UserChildJob(
    val userId: UserId?,
    private val delegate: ChildJob,
) : ChildJob by delegate {
    override fun toString(): String = "UserChildJob(userId='$userId')"
}
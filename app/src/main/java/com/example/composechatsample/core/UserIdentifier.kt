package com.example.composechatsample.core

import com.example.composechatsample.core.models.UserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal class UserIdentifier : AbstractCoroutineContextElement(UserIdentifier) {

    private val _value = MutableStateFlow<UserId?>(null)

    companion object Key : CoroutineContext.Key<UserIdentifier> {
        private const val DEFAULT_TIMEOUT_IN_MS = 10_000L
    }

    var value: UserId?
        get() = _value.value
        set(value) {
            _value.value = value
        }

    suspend fun awaitFor(userId: UserId, timeoutInMs: Long = DEFAULT_TIMEOUT_IN_MS) = runCatching {
        withTimeout(timeoutInMs) {
            _value.first { it == userId }
        }
    }

    override fun toString(): String = "UserIdentifier($value)"
}
package com.example.composechatsample.core

import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.withTimeout

internal class CallPostponeHelper(
    private val userScope: UserScope,
    private val timeoutInMillis: Long = DEFAULT_TIMEOUT,
    private val awaitConnection: suspend () -> Unit,
) {

    private val logger by taggedLogger("Chat:CallPostponeHelper")

    @Suppress("TooGenericExceptionCaught")
    internal fun <T : Any> postponeCall(call: () -> Call<T>): Call<T> {
        return CoroutineCall(userScope) {
            try {
                logger.d { "[postponeCall] no args" }
                withTimeout(timeoutInMillis) {
                    awaitConnection()
                }
                logger.v { "[postponeCall] wait completed" }
                call().await()
            } catch (e: Throwable) {
                logger.e { "[postponeCall] failed: $e" }
                Result.Failure(
                    Error.GenericError(
                        message = "Failed to perform call. Waiting for WS connection was too long.",
                    ),
                )
            }
        }
    }

    companion object {
        private const val DEFAULT_TIMEOUT = 5_000L
    }
}
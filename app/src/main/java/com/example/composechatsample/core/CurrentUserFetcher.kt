package com.example.composechatsample.core

import com.example.composechatsample.core.errors.ChatErrorCode
import com.example.composechatsample.core.events.ConnectedEvent
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.state.NetworkStateProvider
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withTimeoutOrNull
import java.io.UnsupportedEncodingException

internal class CurrentUserFetcher(
    private val networkStateProvider: NetworkStateProvider,
    private val socketFactory: SocketFactory,
    private val config: ChatClientConfig,
) {

    private val logger by taggedLogger("Chat:CurrentUserFetcher")

    suspend fun fetch(currentUser: User): Result<User> {
        logger.d { "[fetch] no args" }
        if (!networkStateProvider.isConnected()) {
            logger.w { "[fetch] rejected (no internet connection)" }
            return Result.Failure(ChatErrorCode.NETWORK_FAILED.toNetworkError())
        }
        var ws: StreamWebSocket? = null
        return try {
            ws = socketFactory.createSocket(currentUser.toConnectionConf(config))
            ws.listen().firstUserWithTimeout(TIMEOUT_MS).also {
                logger.v { "[fetch] completed: $it" }
            }
        } catch (e: UnsupportedEncodingException) {
            logger.e { "[fetch] failed: $e" }
            Result.Failure(Error.ThrowableError(e.message.orEmpty(), e))
        } finally {
            try {
                ws?.close()
            } catch (_: IllegalArgumentException) {
                // no-op
            }
        }
    }

    private fun User.toConnectionConf(config: ChatClientConfig): SocketFactory.ConnectionConf = when (config.isAnonymous) {
        true -> SocketFactory.ConnectionConf.AnonymousConnectionConf(
            config.wssUrl,
            config.apiKey,
            this
        )
        false -> SocketFactory.ConnectionConf.UserConnectionConf(config.wssUrl, config.apiKey, this)
    }.asReconnectionConf()

    private suspend fun Flow<StreamWebSocketEvent>.firstUserWithTimeout(
        timeMillis: Long,
    ): Result<User> = withTimeoutOrNull(timeMillis) {
        mapNotNull {
            when (it) {
                is StreamWebSocketEvent.Error -> Result.Failure(it.streamError)
                is StreamWebSocketEvent.Message -> (it.chatEvent as? ConnectedEvent)?.let { Result.Success(it.me) }
            }
        }
            .first()
    } ?: Result.Failure(Error.GenericError("Timeout while fetching current user"))

    private fun ChatErrorCode.toNetworkError() = Error.NetworkError(
        message = description,
        serverErrorCode = code,
    )

    private companion object {
        private const val TIMEOUT_MS = 15_000L
    }
}
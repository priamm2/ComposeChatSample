package com.example.composechatsample.core

import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.delay

internal class CallRetryService(
  private val retryPolicy: RetryPolicy,
  private val isPermanent: (Error) -> Boolean = { false }
) {

  private val logger by taggedLogger("CallRetryService")

  @Suppress("LoopWithTooManyJumpStatements")
  suspend fun <T : Any> runAndRetry(task: suspend () -> Result<T>): Result<T> {
    var attempt = 1
    var result: Result<T>
    while (true) {
      result = task()
      when (result) {
        is Result.Success -> break
        is Result.Failure -> {
          if (isPermanent.invoke(result.value)) {
            break
          }
          val shouldRetry = retryPolicy.shouldRetry(attempt, result.value)
          val timeout = retryPolicy.retryTimeout(attempt, result.value)

          if (shouldRetry) {
            logger.i {
              "API call failed (attempt $attempt), retrying in $timeout seconds." +
                " Error was ${result.value}"
            }
            delay(timeout.toLong())
            attempt += 1
          } else {
            logger.i {
              "API call failed (attempt $attempt). " +
                "Giving up for now, will retry when connection recovers. " +
                "Error was ${result.value}"
            }
            break
          }
        }
      }
    }
    return result
  }
}
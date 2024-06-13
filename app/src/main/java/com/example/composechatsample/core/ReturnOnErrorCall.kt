package com.example.composechatsample.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

public class ReturnOnErrorCall<T : Any>(
  private val originalCall: Call<T>,
  scope: CoroutineScope,
  private val onErrorReturn: suspend (originalError: Error) -> Result<T>
) : Call<T> {

  private val callScope = scope + SupervisorJob(scope.coroutineContext.job)

  override fun execute(): Result<T> = runBlocking { await() }

  override fun enqueue(callback: Call.Callback<T>) {
    callScope.launch {
      originalCall.enqueue { originalResult ->
        callScope.launch {
          val finalResult = map(originalResult)
          withContext(CallDispatcherProvider.Main) {
            callback.onResult(finalResult)
          }
        }
      }
    }
  }

  override fun cancel() {
    originalCall.cancel()
    callScope.coroutineContext.cancelChildren()
  }

  override suspend fun await(): Result<T> = Call.runCatching(::map) {
    withContext(callScope.coroutineContext) {
      map(originalCall.await())
    }
  }

  private suspend fun map(result: Result<T>): Result<T> = when (result) {
    is Result.Success -> result
    is Result.Failure -> onErrorReturn(result.value)
  }
}
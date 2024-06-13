package com.example.composechatsample.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class DoOnResultCall<T : Any>(
  private val originalCall: Call<T>,
  scope: CoroutineScope,
  private val consumer: suspend (Result<T>) -> Unit
) : Call<T> {

  private val callScope = scope + SupervisorJob(scope.coroutineContext.job)

  override fun execute(): Result<T> = runBlocking { await() }

  override fun enqueue(callback: Call.Callback<T>) {
    callScope.launch {
      originalCall.enqueue { result ->
        callScope.launch {
          withContext(CallDispatcherProvider.Main) {
            callback.onResult(result)
          }
          consumer(result)
        }
      }
    }
  }

  override fun cancel() {
    originalCall.cancel()
    callScope.coroutineContext.cancelChildren()
  }

  override suspend fun await(): Result<T> = Call.runCatching {
    withContext(callScope.coroutineContext) {
      originalCall.await().also { consumer(it) }
    }
  }
}
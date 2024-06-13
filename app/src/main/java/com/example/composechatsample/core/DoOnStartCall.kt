package com.example.composechatsample.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class DoOnStartCall<T : Any>(
  private val originalCall: Call<T>,
  scope: CoroutineScope,
  private val sideEffect: suspend () -> Unit
) : Call<T> {

  private val callScope = scope + SupervisorJob(scope.coroutineContext.job)

  override fun execute(): Result<T> = runBlocking { await() }

  override fun enqueue(callback: Call.Callback<T>) {
    callScope.launch {
      sideEffect()
      originalCall.enqueue {
        callScope.launch(CallDispatcherProvider.Main) { callback.onResult(it) }
      }
    }
  }

  override fun cancel() {
    originalCall.cancel()
    callScope.coroutineContext.cancelChildren()
  }

  override suspend fun await(): Result<T> = Call.runCatching {
    withContext(callScope.coroutineContext) {
      sideEffect()
      originalCall.await()
    }
  }
}
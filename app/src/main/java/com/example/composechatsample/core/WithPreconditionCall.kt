package com.example.composechatsample.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class WithPreconditionCall<T : Any>(
  private val originalCall: Call<T>,
  scope: CoroutineScope,
  private val precondition: suspend () -> Result<Unit>
) : Call<T> {
  private val callScope = scope + SupervisorJob(scope.coroutineContext.job)

  override fun execute(): Result<T> = runBlocking { await() }

  override fun enqueue(callback: Call.Callback<T>) {
    callScope.launch {
      val result = precondition()
      result
        .onSuccess { originalCall.enqueue { callScope.launch { notifyResult(it, callback) } } }
        .onErrorSuspend { notifyResult(Result.Failure(it), callback) }
    }
  }

  private suspend fun notifyResult(result: Result<T>, callback: Call.Callback<T>) =
    withContext(CallDispatcherProvider.Main) {
      callback.onResult(result)
    }

  override fun cancel() {
    originalCall.cancel()
    callScope.coroutineContext.cancelChildren()
  }

  override suspend fun await(): Result<T> = Call.runCatching {
    withContext(callScope.coroutineContext) {
      precondition()
        .flatMapSuspend {
          originalCall.await()
        }
    }
  }
}
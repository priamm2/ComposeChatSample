package com.example.composechatsample.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ErrorCall<T : Any>(
  private val scope: CoroutineScope,
  private val e: Error
) : Call<T> {
  override fun cancel() {
    // Not supported
  }

  override fun execute(): Result<T> {
    return Result.Failure(e)
  }

  override fun enqueue(callback: Call.Callback<T>) {
    scope.launch(CallDispatcherProvider.Main) {
      callback.onResult(Result.Failure(e))
    }
  }

  override suspend fun await(): Result<T> = withContext(scope.coroutineContext) {
    Result.Failure(e)
  }
}
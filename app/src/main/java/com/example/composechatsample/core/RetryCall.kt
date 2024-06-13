package com.example.composechatsample.core

import com.example.composechatsample.core.Call.Companion.callCanceledError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.util.concurrent.atomic.AtomicBoolean

internal class RetryCall<T : Any>(
  private val originalCall: Call<T>,
  private val scope: CoroutineScope,
  private val callRetryService: CallRetryService
) : Call<T> {

  private var job: Job? = null
  private val canceled = AtomicBoolean(false)

  override fun execute(): Result<T> = runBlocking { await() }

  override fun enqueue(callback: Call.Callback<T>) {
    job = scope.launch {
      val result = await()
      withContext(CallDispatcherProvider.Main) {
        yield()
        callback.onResult(result)
      }
    }
  }

  override fun cancel() {
    canceled.set(true)
    originalCall.cancel()
    job?.cancel()
  }

  override suspend fun await(): Result<T> = withContext(scope.coroutineContext) {
    callRetryService.runAndRetry {
      originalCall
        .takeUnless { canceled.get() }
        ?.await()
        ?: callCanceledError()
    }
  }
}
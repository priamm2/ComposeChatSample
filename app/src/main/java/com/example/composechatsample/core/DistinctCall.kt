package com.example.composechatsample.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference

class DistinctCall<T : Any>(
  scope: CoroutineScope,
  private val callBuilder: () -> Call<T>,
  private val onFinished: () -> Unit
) : Call<T> {

  private val distinctScope = scope + SupervisorJob(scope.coroutineContext.job)
  private val deferred = SynchronizedReference<Deferred<Result<T>>>()
  private val delegateCall = AtomicReference<Call<T>>()

  public fun originCall(): Call<T> = callBuilder()

  override fun execute(): Result<T> = runBlocking { await() }

  override fun enqueue(callback: Call.Callback<T>) {
    distinctScope.launch {
      await().takeUnless { it.isCanceled }?.also { result ->
        withContext(CallDispatcherProvider.Main) {
          callback.onResult(result)
        }
      }
    }
  }

  @SuppressWarnings("TooGenericExceptionCaught")
  override suspend fun await(): Result<T> = Call.runCatching {
    deferred.getOrCreate {
      distinctScope.async {
        callBuilder()
          .also { delegateCall.set(it) }
          .await()
          .also { doFinally() }
      }
    }.await()
  }

  override fun cancel() {
    delegateCall.get()?.cancel()
    distinctScope.coroutineContext.cancelChildren()
    doFinally()
  }

  private fun doFinally() {
    if (deferred.reset()) {
      onFinished()
    }
  }

  private val Result<T>.isCanceled get() = this == Call.callCanceledError<T>()
}
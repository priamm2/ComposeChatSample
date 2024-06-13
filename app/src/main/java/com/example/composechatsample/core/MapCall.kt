package com.example.composechatsample.core

import com.example.composechatsample.core.Call.Companion.callCanceledError
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

internal class MapCall<T : Any, K : Any>(
  private val call: Call<T>,
  private val mapper: (T) -> K
) : Call<K> {

  private val canceled = AtomicBoolean(false)

  override fun cancel() {
    canceled.set(true)
    call.cancel()
  }

  override fun execute(): Result<K> = runBlocking { await() }

  override fun enqueue(callback: Call.Callback<K>) {
    call.enqueue {
      it.takeUnless { canceled.get() }
        ?.map(mapper)
        ?.let(callback::onResult)
    }
  }

  override suspend fun await(): Result<K> = withContext(CallDispatcherProvider.IO) {
    call.await()
      .takeUnless { canceled.get() }
      ?.map(mapper)
      .takeUnless { canceled.get() }
      ?: callCanceledError()
  }
}
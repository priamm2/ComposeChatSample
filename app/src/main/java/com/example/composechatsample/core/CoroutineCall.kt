package com.example.composechatsample.core

import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

public class CoroutineCall<T : Any>(
  private val scope: CoroutineScope,
  private val suspendingTask: suspend CoroutineScope.() -> Result<T>
) : Call<T> {

  private val logger by taggedLogger("CoroutineCall")

  private val jobs = hashSetOf<Job>()

  override fun execute(): Result<T> = runBlocking { await() }

  override suspend fun await(): Result<T> = Call.runCatching {
    logger.d { "[await] no args" }
    withContext(scope.coroutineContext) {
      jobs.addFrom(coroutineContext)
      suspendingTask()
    }
  }

  override fun cancel() {
    logger.d { "[cancel] no args" }
    jobs.cancelAll()
  }

  override fun enqueue(callback: Call.Callback<T>) {
    logger.d { "[enqueue] no args" }
    scope.launch {
      jobs.addFrom(coroutineContext)
      val result = suspendingTask()
      withContext(CallDispatcherProvider.Main) {
        callback.onResult(result)
      }
    }
  }

  private fun HashSet<Job>.cancelAll() {
    synchronized(this) {
      forEach {
        it.cancel()
      }
      clear()
    }
  }

  private fun HashSet<Job>.addFrom(context: CoroutineContext) {
    synchronized(this) {
      context[Job]?.also {
        add(it)
      }
    }
  }
}
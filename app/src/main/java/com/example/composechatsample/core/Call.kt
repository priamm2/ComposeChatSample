package com.example.composechatsample.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.coroutines.cancellation.CancellationException

public interface Call<T : Any> {

  public fun execute(): Result<T>

  public fun enqueue(callback: Callback<T>)

  public fun enqueue(): Unit = enqueue {}

  public suspend fun await(): Result<T>

  public fun cancel()

  public fun interface Callback<T : Any> {
    public fun onResult(result: Result<T>)
  }

  public companion object {
    public fun <T : Any> callCanceledError(): Result<T> =
      Result.Failure(Error.GenericError(message = "The call was canceled before complete its execution."))

    @SuppressWarnings("TooGenericExceptionCaught")
    public suspend fun <T : Any> runCatching(
      errorMap: suspend (originalResultError: Result<T>) -> Result<T> = { it },
      block: suspend () -> Result<T>
    ): Result<T> = try {
      block().also { yield() }
    } catch (t: Throwable) {
      errorMap(t.toResult())
    }

    private fun <T : Any> Throwable.toResult(): Result<T> = when (this) {
      is CancellationException -> callCanceledError()
      else -> Result.Failure(Error.ThrowableError(message = "", cause = this))
    }
  }
}

public fun <T : Any> Call<T>.launch(scope: CoroutineScope) {
  scope.launch coroutineScope@{
    this@launch.await()
  }
}

public fun <T : Any, K : Any> Call<T>.map(mapper: (T) -> K): Call<K> {
  return MapCall(this, mapper)
}

public fun <T : Any, K : Any> Call<T>.zipWith(call: Call<K>): Call<Pair<T, K>> {
  return ZipCall(this, call)
}

public fun <T : Any> Call<T>.doOnStart(
  scope: CoroutineScope,
  function: suspend () -> Unit
): Call<T> =
  DoOnStartCall(this, scope, function)

public fun <T : Any> Call<T>.doOnResult(
  scope: CoroutineScope,
  function: suspend (Result<T>) -> Unit
): Call<T> =
  DoOnResultCall(this, scope, function)

public fun <T : Any> Call<T>.withPrecondition(
  scope: CoroutineScope,
  precondition: suspend () -> Result<Unit>
): Call<T> =
  WithPreconditionCall(this, scope, precondition)

public fun <T : Any> Call<T>.onErrorReturn(
  scope: CoroutineScope,
  function: suspend (originalError: Error) -> Result<T>
): ReturnOnErrorCall<T> = ReturnOnErrorCall(this, scope, function)

public fun <T : Any> Call<T>.share(
  scope: CoroutineScope,
  identifier: () -> Int
): Call<T> {
  return SharedCall(this, identifier, scope)
}

public fun Call<*>.toUnitCall(): Call<Unit> = map {}

private val onSuccessStub: (Any) -> Unit = {}
private val onErrorStub: (Error) -> Unit = {}

public fun <T : Any> Call<T>.enqueue(
  onSuccess: (T) -> Unit = onSuccessStub,
  onError: (Error) -> Unit = onErrorStub
) {
  enqueue { result ->
    when (result) {
      is Result.Success -> onSuccess(result.value)
      is Result.Failure -> onError(result.value)
    }
  }
}

public fun <T : Any> Call<T>.retry(scope: CoroutineScope, retryPolicy: RetryPolicy): Call<T> =
  RetryCall(this, scope, CallRetryService(retryPolicy))

public fun <T : Any> Call<T>.forceNewRequest(): Call<T> {
  return if (this is DistinctCall) {
    this.originCall()
  } else {
    this
  }
}
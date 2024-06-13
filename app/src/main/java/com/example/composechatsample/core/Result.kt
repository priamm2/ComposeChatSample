package com.example.composechatsample.core

public sealed class Result<out A : Any> {

  val isSuccess: Boolean
    inline get() = this is Success

  public val isFailure: Boolean
    inline get() = this is Failure

  public fun getOrNull(): A? = when (this) {
    is Success -> value
    is Failure -> null
  }

  @Throws(IllegalStateException::class)
  public fun getOrThrow(): A = when (this) {
    is Success -> value
    is Failure -> throw IllegalStateException("The Success::value cannot be accessed as the Result is a Failure.")
  }

  public fun errorOrNull(): Error? = when (this) {
    is Success -> null
    is Failure -> value
  }

  public data class Success<out A : Any>(val value: A) : Result<A>()

  public data class Failure(val value: Error) : Result<Nothing>()

  public inline fun <C : Any> map(f: (A) -> C): Result<C> = flatMap { Success(f(it)) }

  @JvmSynthetic
  public suspend inline fun <C : Any> mapSuspend(crossinline f: suspend (A) -> C): Result<C> =
    flatMap { Success(f(it)) }

  public fun toUnitResult(): Result<Unit> = map {}

  public inline fun onSuccess(
    crossinline successSideEffect: (A) -> Unit
  ): Result<A> =
    also {
      when (it) {
        is Success -> successSideEffect(it.value)
        is Failure -> Unit
      }
    }

  public inline fun onError(
    crossinline errorSideEffect: (Error) -> Unit
  ): Result<A> =
    also {
      when (it) {
        is Success -> Unit
        is Failure -> errorSideEffect(it.value)
      }
    }
}


public inline fun <A : Any, C : Any> Result<A>.flatMap(f: (A) -> Result<C>): Result<C> {
  return when (this) {
    is Result.Success -> f(this.value)
    is Result.Failure -> this
  }
}

@JvmSynthetic
public suspend inline fun <A : Any, C : Any> Result<A>.flatMapSuspend(
  crossinline f: suspend (A) -> Result<C>
): Result<C> {
  return when (this) {
    is Result.Success -> f(this.value)
    is Result.Failure -> this
  }
}

@JvmSynthetic
public suspend inline fun <A : Any> Result<A>.onSuccessSuspend(
  crossinline successSideEffect: suspend (A) -> Unit
): Result<A> =
  also {
    when (it) {
      is Result.Success -> successSideEffect(it.value)
      is Result.Failure -> Unit
    }
  }

@JvmSynthetic
public suspend inline fun <A : Any> Result<A>.onErrorSuspend(
  crossinline errorSideEffect: suspend (Error) -> Unit
): Result<A> =
  also {
    when (it) {
      is Result.Success -> Unit
      is Result.Failure -> errorSideEffect(it.value)
    }
  }

@JvmSynthetic
public fun <A : Any> Result<A>.recover(errorMapper: (Error) -> A): Result.Success<A> {
  return when (this) {
    is Result.Success -> this
    is Result.Failure -> Result.Success(errorMapper(value))
  }
}

@JvmSynthetic
public suspend inline fun <A : Any> Result<A>.recoverSuspend(
  crossinline errorMapper: suspend (Error) -> A
): Result.Success<A> {
  return when (this) {
    is Result.Success -> this
    is Result.Failure -> Result.Success(errorMapper(value))
  }
}

@JvmSynthetic
public inline infix fun <T : Any, U : Any> Result<T>.then(f: (T) -> Result<U>): Result<U> =
  when (this) {
    is Result.Success -> f(this.value)
    is Result.Failure -> Result.Failure(this.value)
  }
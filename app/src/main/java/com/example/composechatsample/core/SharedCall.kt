package com.example.composechatsample.core

import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

internal fun <T : Any> SharedCall(
  origin: Call<T>,
  originIdentifier: () -> Int,
  scope: CoroutineScope
): Call<T> {
  val sharedCalls = scope.coroutineContext[SharedCalls] ?: return origin
  val identifier = originIdentifier()
  return sharedCalls[identifier] as? Call<T>
    ?: DistinctCall(scope, { origin }) {
      sharedCalls.remove(identifier)
    }.also {
      sharedCalls.put(identifier, it)
    }
}

public class SharedCalls : CoroutineContext.Element {

  public override val key: CoroutineContext.Key<SharedCalls> = Key

  private val calls = ConcurrentHashMap<Int, Call<out Any>>()

  internal operator fun get(identifier: Int): Call<out Any>? {
    return calls[identifier]
  }

  internal fun put(identifier: Int, value: Call<out Any>) {
    calls[identifier] = value
  }

  internal fun remove(identifier: Int) {
    calls.remove(identifier)
  }

  public companion object Key : CoroutineContext.Key<SharedCalls>
}
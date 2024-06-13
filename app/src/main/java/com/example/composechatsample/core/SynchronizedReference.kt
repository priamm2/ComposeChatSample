package com.example.composechatsample.core

internal class SynchronizedReference<T : Any>(
  @Volatile private var value: T? = null
) {

  fun get(): T? = value

  fun getOrCreate(builder: () -> T): T {
    return value ?: synchronized(this) {
      value ?: builder.invoke().also {
        value = it
      }
    }
  }

  fun reset(): Boolean {
    return set(null) != null
  }

  fun set(value: T?): T? {
    synchronized(this) {
      val currentValue = this.value
      this.value = value
      return currentValue
    }
  }
}
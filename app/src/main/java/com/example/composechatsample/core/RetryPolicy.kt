package com.example.composechatsample.core

public interface RetryPolicy {

  public fun shouldRetry(attempt: Int, error: Error): Boolean
  public fun retryTimeout(attempt: Int, error: Error): Int
}
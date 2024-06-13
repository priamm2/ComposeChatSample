package com.example.composechatsample.core

internal class NoRetryPolicy : RetryPolicy {
    override fun shouldRetry(attempt: Int, error: Error): Boolean = false
    override fun retryTimeout(attempt: Int, error: Error): Int = 0
}
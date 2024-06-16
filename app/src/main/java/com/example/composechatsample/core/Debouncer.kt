package com.example.composechatsample.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

public class Debouncer(
    private val debounceMs: Long,
    private val scope: CoroutineScope = CoroutineScope(DispatcherProvider.Main),
) {

    private var job: Job? = null


    public fun submit(work: () -> Unit) {
        job?.cancel()
        job = scope.launch {
            delay(debounceMs)
            work()
        }
    }

    public fun submitSuspendable(work: suspend () -> Unit) {
        job?.cancel()
        job = scope.launch {
            delay(debounceMs)
            work()
        }
    }

    public fun cancelLastDebounce() {
        job?.cancel()
    }

    public fun shutdown() {
        scope.cancel()
    }
}
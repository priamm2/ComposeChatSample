package com.example.composechatsample.core.plugin

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

public class Tube<T> : Flow<T>, FlowCollector<T> {

    private val mutex = Mutex()
    private val collectors = hashSetOf<FlowCollector<T>>()

    override suspend fun collect(collector: FlowCollector<T>) {
        try {
            mutex.withLock {
                collectors.add(collector)
            }
            awaitCancellation()
        } catch (_: Throwable) {
        } finally {
            mutex.withLock {
                collectors.remove(collector)
            }
        }
    }


    override suspend fun emit(value: T) {
        mutex.withLock {
            collectors.forEach {
                try {
                    it.emit(value)
                } catch (_: Throwable) {

                }
            }
        }
    }
}
package com.example.composechatsample.core.repository

import androidx.annotation.VisibleForTesting
import com.example.composechatsample.core.events.ChatEvent
import com.example.composechatsample.core.events.ConnectedEvent
import com.example.composechatsample.core.events.ConnectingEvent
import com.example.composechatsample.core.events.DisconnectedEvent
import com.example.composechatsample.core.plugin.BatchEvent
import com.example.composechatsample.core.realType
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class SocketEventCollector @VisibleForTesting constructor(
    private val scope: CoroutineScope,
    private val timeout: Long,
    private val timeLimit: Long,
    private val itemCountLimit: Int,
    now: () -> Long,
    private val fireEvent: suspend (BatchEvent) -> Unit,
) {
    constructor(
        scope: CoroutineScope,
        fireEvent: suspend (BatchEvent) -> Unit,
    ) : this(
        scope = scope,
        timeout = TIMEOUT,
        timeLimit = TIME_LIMIT,
        itemCountLimit = ITEM_COUNT_LIMIT,
        now = { System.currentTimeMillis() },
        fireEvent = fireEvent,
    )

    private val logger by taggedLogger(TAG)
    private val mutex = Mutex()
    private val postponed = Postponed(now)
    private val timeoutJob = TimeoutJob()

    internal suspend fun collect(event: ChatEvent) {
        logger.d { "[collect] event.type: '${event.realType}', event.hash: ${event.hashCode()}" }
        if (add(event)) {
            return
        }
        firePostponed()
        val batchEvent = BatchEvent(sortedEvents = listOf(event), isFromHistorySync = false)
        fireEvent(batchEvent)
    }

    private suspend fun add(event: ChatEvent): Boolean {
        if (event !is ConnectingEvent && event !is ConnectedEvent && event !is DisconnectedEvent) {
            logger.d { "[add] event.type: ${event.realType}(${event.hashCode()})" }
            mutex.withLock {
                timeoutJob.cancel()
                return postponed.add(event).also {
                    when {
                        postponed.size >= itemCountLimit -> onItemCountLimit()
                        postponed.collectionTime() >= timeLimit -> onTimeLimit()
                        else -> scheduleTimeout()
                    }
                }
            }
        }
        logger.v { "[add] rejected (unsupported event.type): ${event.realType}" }
        return false
    }

    private suspend fun firePostponed() {
        logger.d { "[firePostponed] no args" }
        mutex.withLock {
            timeoutJob.cancel()
            doFire()
        }
    }

    private fun scheduleTimeout() {
        timeoutJob.set(
            scope.launch {
                delay(timeout)
                logger.i { "[scheduleTimeout] timeout is triggered" }
                mutex.withLock {
                    doFire()
                }
            },
        )
    }

    private suspend fun onItemCountLimit() {
        logger.i { "[onItemCountLimit] no args" }
        doFire()
    }

    private suspend fun onTimeLimit() {
        logger.i { "[onTimeLimit] no args" }
        doFire()
    }

    private suspend fun doFire() {
        if (postponed.isEmpty()) {
            logger.v { "[doFire] rejected (postponed is empty)" }
            return
        }
        logger.v { "[doFire] postponed.size: ${postponed.size}" }
        val sortedEvents = postponed.sortedBy { it.createdAt }
        postponed.clear()
        timeoutJob.reset()
        fireEvent(
            BatchEvent(sortedEvents = sortedEvents, isFromHistorySync = false),
        )
    }

    private companion object {
        private const val TAG = "Chat:EventCollector"
        private const val TIMEOUT = 300L
        private const val TIME_LIMIT = 1000L
        private const val ITEM_COUNT_LIMIT = 200
    }
}

private class Postponed(
    private val now: () -> Long,
) {

    private val events = arrayListOf<ChatEvent>()
    private var collectStartTime = ZERO

    val size: Int get() = events.size

    fun collectionTime(): Long = when (val time = collectStartTime) {
        ZERO -> ZERO
        else -> now() - time
    }

    fun add(event: ChatEvent): Boolean {
        if (events.isEmpty()) {
            collectStartTime = now()
        }
        return events.add(event)
    }

    fun <R : Comparable<R>> sortedBy(predicate: (ChatEvent) -> R) = events.sortedBy(predicate)

    fun isEmpty(): Boolean = events.isEmpty()

    fun clear() {
        events.clear()
        collectStartTime = ZERO
    }

    private companion object {
        private const val ZERO = 0L
    }
}

private class TimeoutJob {

    private var pending: Job? = null

    fun cancel() {
        pending?.cancel()
        pending = null
    }

    fun reset() {
        pending = null
    }

    fun set(job: Job) {
        pending?.cancel()
        pending = job
    }
}
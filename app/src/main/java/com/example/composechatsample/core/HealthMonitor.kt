package com.example.composechatsample.core

import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

internal class HealthMonitor(
    private val timeProvider: TimeProvider = TimeProvider,
    private val retryInterval: RetryInterval = ExponencialRetryInterval,
    private val userScope: UserScope,
    private val checkCallback: suspend () -> Unit,
    private val reconnectCallback: suspend () -> Unit,
) {

    private var consecutiveFailures = 0
    private var lastAck: Long = 0
    private var healthMonitorJob: Job? = null
    private var healthCheckJob: Job? = null
    private var reconnectJob: Job? = null

    private val logger by taggedLogger("Chat:SocketMonitor")

    fun stop() {
        stopAllJobs()
    }

    fun ack() {
        resetHealthMonitor()
    }

    fun onDisconnected() {
        stopAllJobs()
        lastAck = 0
        postponeReconnect()
    }

    private fun resetHealthMonitor() {
        stopAllJobs()
        lastAck = timeProvider.provideCurrentTimeInMilliseconds()
        consecutiveFailures = 0
        postponeHealthMonitor()
    }

    private fun postponeHealthMonitor() {
        healthMonitorJob?.cancel()
        healthMonitorJob = userScope.launchDelayed(MONITOR_INTERVAL) {
            if (needToReconnect()) {
                postponeReconnect()
            } else {
                postponeHealthCheck()
            }
        }
    }

    private fun postponeHealthCheck() {
        healthCheckJob?.cancel()
        healthCheckJob = userScope.launchDelayed(HEALTH_CHECK_INTERVAL) {
            checkCallback()
            postponeHealthMonitor()
        }
    }

    private fun postponeReconnect() {
        reconnectJob?.cancel()
        val retryIntervalTime = retryInterval.nextInterval(consecutiveFailures++)
        logger.i { "Next connection attempt in $retryIntervalTime ms" }
        reconnectJob = userScope.launchDelayed(retryIntervalTime) {
            reconnectCallback()
            postponeHealthMonitor()
        }
    }

    private fun stopAllJobs() {
        reconnectJob?.cancel()
        healthCheckJob?.cancel()
        healthMonitorJob?.cancel()
    }

    private fun needToReconnect(): Boolean =
        (timeProvider.provideCurrentTimeInMilliseconds() - lastAck) >= NO_EVENT_INTERVAL_THRESHOLD

    private fun CoroutineScope.launchDelayed(
        delayMilliseconds: Long,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = launch {
        delay(delayMilliseconds)
        block()
    }

    internal fun interface RetryInterval {
        fun nextInterval(consecutiveFailures: Int): Long
    }

    object ExponencialRetryInterval : RetryInterval {

        @Suppress("MagicNumber")
        override fun nextInterval(consecutiveFailures: Int): Long {
            val max = min(500 + consecutiveFailures * 2000, 25000)
            val min = min(
                max(250, (consecutiveFailures - 1) * 2000),
                25000,
            )
            return floor(Math.random() * (max - min) + min).toLong()
        }
    }
}
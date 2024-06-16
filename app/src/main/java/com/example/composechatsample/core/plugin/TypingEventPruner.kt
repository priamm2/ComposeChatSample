package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.events.TypingStartEvent
import com.example.composechatsample.core.models.TypingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class TypingEventPruner(
    private val channelId: String,
    private val coroutineScope: CoroutineScope,
    private val delayTimeMs: Long = DEFAULT_DELAY_TIME_MS,
    private inline val onUpdated: (
        rawTypingEvents: Map<String, TypingStartEvent>,
        typingEvent: TypingEvent,
    ) -> Unit,
) {

    private val typingEvents = mutableMapOf<String, TimedTypingStartEvent>()

    fun processEvent(
        userId: String,
        typingStartEvent: TypingStartEvent?,
    ) {
        when (typingStartEvent) {
            null -> removeTypingEvent(userId)
            else -> addTypingEvent(
                userId = userId,
                typingStartEvent = typingStartEvent,
            )
        }
    }

    private fun addTypingEvent(userId: String, typingStartEvent: TypingStartEvent) {
        val timedTypingStartEvent = TimedTypingStartEvent(
            coroutineScope = coroutineScope,
            typingStartEvent = typingStartEvent,
            userId = userId,
            delayTimeMs = DEFAULT_DELAY_TIME_MS,
            removeTypingEvent = {
                removeTypingEvent(it)
            },
        )

        typingEvents[userId]?.cancelJob()
        typingEvents[userId] = timedTypingStartEvent
        onUpdated(getRawTyping(), getTypingEvent())
    }

    private fun removeTypingEvent(userId: String) {
        typingEvents[userId]?.cancelJob()

        typingEvents.remove(userId)
        onUpdated(getRawTyping(), getTypingEvent())
    }

    private fun getRawTyping(): Map<String, TypingStartEvent> = typingEvents.mapValues { it.value.typingStartEvent }

    private fun getTypingEvent(): TypingEvent = typingEvents.values
        .map { it.typingStartEvent }
        .sortedBy { typingStartEvent -> typingStartEvent.createdAt }
        .map { typingStartEvent -> typingStartEvent.user }
        .let { sortedUsers -> TypingEvent(channelId = channelId, users = sortedUsers) }

    private fun clear() {
        typingEvents.forEach { it.value.cancelJob() }
        typingEvents.clear()

        onUpdated(getRawTyping(), getTypingEvent())
    }

    companion object {
        const val DEFAULT_DELAY_TIME_MS = 7000L
    }
}

internal data class TimedTypingStartEvent(
    private val coroutineScope: CoroutineScope,
    internal val typingStartEvent: TypingStartEvent,
    private val userId: String,
    private val delayTimeMs: Long,
    private inline val removeTypingEvent: (userId: String) -> Unit,
) {

    private var job: Job? = null

    init {
        job = coroutineScope.launch {
            delay(delayTimeMs)
            removeTypingEvent(userId)
        }
    }

    fun cancelJob() {
        job?.cancel()
    }
}
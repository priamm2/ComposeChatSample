package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.events.ChatEvent
import java.util.concurrent.atomic.AtomicInteger

private val idGenerator = AtomicInteger(0)

internal class BatchEvent(
    val id: Int = idGenerator.incrementAndGet(),
    val sortedEvents: List<ChatEvent>,
    val isFromHistorySync: Boolean,
) {
    val size: Int = sortedEvents.size
    val isFromSocketConnection: Boolean = !isFromHistorySync
}
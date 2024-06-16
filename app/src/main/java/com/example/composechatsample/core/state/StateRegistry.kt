package com.example.composechatsample.core.state

import com.example.composechatsample.core.ChannelState
import com.example.composechatsample.core.QueryChannelsState
import com.example.composechatsample.core.events.ChannelDeletedEvent
import com.example.composechatsample.core.events.NotificationChannelDeletedEvent
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.querysort.QuerySorter
import com.example.composechatsample.core.plugin.ChannelMutableState
import com.example.composechatsample.core.plugin.QueryChannelsMutableState
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap

public class StateRegistry constructor(
    private val userStateFlow: StateFlow<User?>,
    private var latestUsers: StateFlow<Map<String, User>>,
    private val job: Job,
    private val scope: CoroutineScope,
) {

    private val logger by taggedLogger("Chat:StateRegistry")

    private val queryChannels: ConcurrentHashMap<Pair<FilterObject, QuerySorter<Channel>>, QueryChannelsMutableState> =
        ConcurrentHashMap()
    private val channels: ConcurrentHashMap<Pair<String, String>, ChannelMutableState> = ConcurrentHashMap()
    private val threads: ConcurrentHashMap<String, ThreadMutableState> = ConcurrentHashMap()

    public fun queryChannels(filter: FilterObject, sort: QuerySorter<Channel>): QueryChannelsState {
        return queryChannels.getOrPut(filter to sort) {
            QueryChannelsMutableState(filter, sort, scope, latestUsers)
        }
    }

    public fun channel(channelType: String, channelId: String): ChannelState = mutableChannel(channelType, channelId)

    internal fun mutableChannel(channelType: String, channelId: String): ChannelMutableState {
        return channels.getOrPut(channelType to channelId) {
            ChannelMutableState(channelType, channelId, userStateFlow, latestUsers)
        }
    }

    internal fun markChannelAsRead(channelType: String, channelId: String): Boolean =
        mutableChannel(channelType = channelType, channelId = channelId).markChannelAsRead()

    internal fun isActiveChannel(channelType: String, channelId: String): Boolean {
        return channels.containsKey(channelType to channelId)
    }

    public fun thread(messageId: String): ThreadState = mutableThread(messageId)

    internal fun mutableThread(messageId: String): ThreadMutableState = threads.getOrPut(messageId) {
        ThreadMutableState(messageId, scope)
    }

    internal fun getActiveQueryChannelsStates(): List<QueryChannelsState> = queryChannels.values.toList()

    internal fun getActiveChannelStates(): List<ChannelState> = channels.values.toList()

    public fun clear() {
        job.cancelChildren()
        queryChannels.forEach { it.value.destroy() }
        queryChannels.clear()
        channels.forEach { it.value.destroy() }
        channels.clear()
        threads.forEach { it.value.destroy() }
        threads.clear()
    }

    internal fun handleBatchEvent(batchEvent: BatchEvent) {
        for (event in batchEvent.sortedEvents) {
            when (event) {
                is ChannelDeletedEvent -> {
                    removeChanel(event.channelType, event.channelId)
                }
                is NotificationChannelDeletedEvent -> {
                    removeChanel(event.channelType, event.channelId)
                }
                else -> continue
            }
        }
    }

    private fun removeChanel(channelType: String, channelId: String) {
        val removed = channels.remove(channelType to channelId)?.also {
            it.destroy()
        }
        logger.i { "[removeChanel] removed channel($channelType, $channelId): $removed" }
    }
}
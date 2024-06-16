package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.events.ChannelDeletedEvent
import com.example.composechatsample.core.events.ChannelHiddenEvent
import com.example.composechatsample.core.events.ChannelVisibleEvent
import com.example.composechatsample.core.events.ChatEvent
import com.example.composechatsample.core.events.CidEvent
import com.example.composechatsample.core.events.HasChannel
import com.example.composechatsample.core.events.NotificationChannelDeletedEvent
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject

public fun interface ChatEventHandler {
    public fun handleChatEvent(event: ChatEvent, filter: FilterObject, cachedChannel: Channel?): EventHandlingResult
}

public sealed class EventHandlingResult {
    public data class Add(public val channel: Channel) : EventHandlingResult()

    public data class WatchAndAdd(public val cid: String) : EventHandlingResult()

    public data class Remove(public val cid: String) : EventHandlingResult()

    public object Skip : EventHandlingResult() {
        override fun toString(): String = "Skip"
    }
}

public abstract class BaseChatEventHandler : ChatEventHandler {

    public open fun handleChannelEvent(event: HasChannel, filter: FilterObject): EventHandlingResult {
        return when (event) {
            is ChannelDeletedEvent -> EventHandlingResult.Remove(event.cid)
            is NotificationChannelDeletedEvent -> EventHandlingResult.Remove(event.cid)
            else -> EventHandlingResult.Skip
        }
    }

    public open fun handleCidEvent(
        event: CidEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult {
        return when (event) {
            is ChannelHiddenEvent -> EventHandlingResult.Remove(event.cid)
            is ChannelVisibleEvent -> EventHandlingResult.WatchAndAdd(event.cid)
            else -> EventHandlingResult.Skip
        }
    }

    override fun handleChatEvent(event: ChatEvent, filter: FilterObject, cachedChannel: Channel?): EventHandlingResult {
        return when (event) {
            is HasChannel -> handleChannelEvent(event, filter)
            is CidEvent -> handleCidEvent(event, filter, cachedChannel)
            else -> EventHandlingResult.Skip
        }
    }
}
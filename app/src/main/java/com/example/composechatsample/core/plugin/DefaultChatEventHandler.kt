package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.events.CidEvent
import com.example.composechatsample.core.events.HasChannel
import com.example.composechatsample.core.events.MemberAddedEvent
import com.example.composechatsample.core.events.MemberRemovedEvent
import com.example.composechatsample.core.events.NewMessageEvent
import com.example.composechatsample.core.events.NotificationAddedToChannelEvent
import com.example.composechatsample.core.events.NotificationMessageNewEvent
import com.example.composechatsample.core.events.NotificationRemovedFromChannelEvent
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.state.ClientState
import kotlinx.coroutines.flow.StateFlow

public open class DefaultChatEventHandler(
    protected val channels: StateFlow<Map<String, Channel>?>,
    protected val clientState: ClientState,
) : BaseChatEventHandler() {


    override fun handleCidEvent(event: CidEvent, filter: FilterObject, cachedChannel: Channel?): EventHandlingResult {
        return when (event) {
            is NewMessageEvent -> handleNewMessageEvent(event, cachedChannel)
            is MemberRemovedEvent -> removeIfCurrentUserLeftChannel(event.cid, event.member)
            is MemberAddedEvent -> addIfCurrentUserJoinedChannel(cachedChannel, event.member)
            else -> super.handleCidEvent(event, filter, cachedChannel)
        }
    }


    override fun handleChannelEvent(event: HasChannel, filter: FilterObject): EventHandlingResult {
        return when (event) {
            is NotificationMessageNewEvent -> EventHandlingResult.WatchAndAdd(event.cid)
            is NotificationAddedToChannelEvent -> EventHandlingResult.WatchAndAdd(event.cid)
            is NotificationRemovedFromChannelEvent -> removeIfCurrentUserLeftChannel(event.cid, event.member)
            else -> super.handleChannelEvent(event, filter)
        }
    }

    private fun handleNewMessageEvent(event: NewMessageEvent, cachedChannel: Channel?): EventHandlingResult {
        return if (event.message.type == SYSTEM_MESSAGE) {
            EventHandlingResult.Skip
        } else {
            addIfChannelIsAbsent(cachedChannel)
        }
    }

    private fun removeIfCurrentUserLeftChannel(cid: String, member: Member): EventHandlingResult {
        return if (member.getUserId() != clientState.user.value?.id) {
            EventHandlingResult.Skip
        } else {
            removeIfChannelExists(cid)
        }
    }

    protected fun removeIfChannelExists(cid: String): EventHandlingResult {
        val channelsMap = channels.value

        return when {
            channelsMap == null -> EventHandlingResult.Skip
            channelsMap.containsKey(cid) -> EventHandlingResult.Remove(cid)
            else -> EventHandlingResult.Skip
        }
    }

    protected fun addIfCurrentUserJoinedChannel(channel: Channel?, member: Member): EventHandlingResult {
        return if (clientState.user.value?.id == member.getUserId()) {
            addIfChannelIsAbsent(channel)
        } else {
            EventHandlingResult.Skip
        }
    }

    protected fun addIfChannelIsAbsent(channel: Channel?): EventHandlingResult {
        val channelsMap = channels.value

        return when {
            channelsMap == null || channel == null -> EventHandlingResult.Skip
            channelsMap.containsKey(channel.cid) -> EventHandlingResult.Skip
            else -> EventHandlingResult.Add(channel)
        }
    }

    private companion object {
        private const val SYSTEM_MESSAGE = "system"
    }
}
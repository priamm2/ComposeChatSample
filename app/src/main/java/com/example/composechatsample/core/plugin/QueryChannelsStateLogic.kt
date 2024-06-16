package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.QueryChannelsSpec
import com.example.composechatsample.core.QueryChannelsState
import com.example.composechatsample.core.api.QueryChannelsRequest
import com.example.composechatsample.core.cidToTypeAndId
import com.example.composechatsample.core.events.ChatEvent
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.state.StateRegistry
import com.example.composechatsample.core.toCid
import com.example.composechatsample.core.users
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

class QueryChannelsStateLogic(
    private val mutableState: QueryChannelsMutableState,
    private val stateRegistry: StateRegistry,
    private val logicRegistry: LogicRegistry,
    private val coroutineScope: CoroutineScope,
) {

    private val logger by taggedLogger("QueryChannelsStateLogic")

    internal fun handleChatEvent(event: ChatEvent, cachedChannel: Channel?): EventHandlingResult {
        return mutableState.handleChatEvent(event, cachedChannel)
    }

    internal fun isLoading(): Boolean = mutableState.currentLoading.value

    internal fun getChannelsOffset(): Int = mutableState.channelsOffset.value

    internal fun getChannels(): Map<String, Channel>? = mutableState.rawChannels

    internal fun getQuerySpecs(): QueryChannelsSpec = mutableState.queryChannelsSpec

    internal fun getState(): QueryChannelsState = mutableState

    internal fun setLoadingMore(isLoading: Boolean) {
        mutableState.setLoadingMore(isLoading)
    }

    internal fun setLoadingFirstPage(isLoading: Boolean) {
        mutableState.setLoadingFirstPage(isLoading)
    }

    internal fun setCurrentRequest(request: QueryChannelsRequest) {
        logger.d { "[onQueryChannelsRequest] request: $request" }
        mutableState.setCurrentRequest(request)
    }

    internal fun setEndOfChannels(isEnd: Boolean) {
        mutableState.setEndOfChannels(isEnd)
    }

    internal fun setRecoveryNeeded(recoveryNeeded: Boolean) {
        mutableState.setRecoveryNeeded(recoveryNeeded)
    }

    internal fun setChannelsOffset(offset: Int) {
        mutableState.setChannelsOffset(offset)
    }

    internal fun incrementChannelsOffset(size: Int) {
        val currentChannelsOffset = mutableState.channelsOffset.value
        val newChannelsOffset = currentChannelsOffset + size
        logger.v { "[updateOnlineChannels] newChannelsOffset: $newChannelsOffset <= $currentChannelsOffset" }
        mutableState.setChannelsOffset(newChannelsOffset)
    }

    internal suspend fun addChannelsState(channels: List<Channel>) {
        mutableState.queryChannelsSpec.cids += channels.map { it.cid }
        val existingChannels = mutableState.rawChannels
        mutableState.setChannels((existingChannels ?: emptyMap()) + channels.map { it.cid to it })
        channels.map { channel ->
            coroutineScope.async {
                logicRegistry.channelState(channel.type, channel.id).updateDataForChannel(
                    channel = channel,
                    messageLimit = channel.messages.size,
                    isChannelsStateUpdate = true,
                )
            }
        }.map {
            it.await()
        }
    }

    internal fun removeChannels(cidSet: Set<String>) {
        val existingChannels = mutableState.rawChannels
        if (existingChannels == null) {
            logger.w { "[removeChannels] rejected (existingChannels is null)" }
            return
        }
        mutableState.queryChannelsSpec.cids = mutableState.queryChannelsSpec.cids - cidSet
        mutableState.setChannels(existingChannels - cidSet)
    }

    internal fun initializeChannelsIfNeeded() {
        if (mutableState.rawChannels == null) {
            mutableState.setChannels(emptyMap())
        }
    }

    internal fun refreshChannels(cidList: Collection<String>) {
        val existingChannels = mutableState.rawChannels
        if (existingChannels == null) {
            logger.w { "[refreshChannels] rejected (existingChannels is null)" }
            return
        }

        val newChannels = existingChannels + mutableState.queryChannelsSpec.cids
            .intersect(cidList.toSet())
            .map { cid -> cid.cidToTypeAndId() }
            .filter { (channelType, channelId) ->
                stateRegistry.isActiveChannel(
                    channelType = channelType,
                    channelId = channelId,
                )
            }
            .associate { (channelType, channelId) ->
                val cid = (channelType to channelId).toCid()
                cid to stateRegistry.channel(
                    channelType = channelType,
                    channelId = channelId,
                ).toChannel()
            }

        mutableState.setChannels(newChannels)
    }


    internal fun refreshMembersStateForUser(newUser: User) {
        val userId = newUser.id
        val existingChannels = mutableState.rawChannels

        if (existingChannels == null) {
            logger.w { "[refreshMembersStateForUser] rejected (existingChannels is null)" }
            return
        }

        val affectedChannels = existingChannels
            .filter { (_, channel) -> channel.users().any { it.id == userId } }
            .mapValues { (_, channel) ->
                channel.copy(
                    members = channel.members.map { member ->
                        member.copy(user = member.user.takeUnless { it.id == userId } ?: newUser)
                    },
                )
            }

        mutableState.setChannels(existingChannels + affectedChannels)
    }
}
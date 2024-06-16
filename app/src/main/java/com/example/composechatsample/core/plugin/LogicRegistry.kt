package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.ChannelStateLogicProvider
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.api.QueryChannelsRequest
import com.example.composechatsample.core.cidToTypeAndId
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.querysort.QuerySorter
import com.example.composechatsample.core.repository.RepositoryFacade
import com.example.composechatsample.core.state.ClientState
import com.example.composechatsample.core.state.StateRegistry
import com.example.composechatsample.core.state.ThreadStateLogic
import com.example.composechatsample.core.toMutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class LogicRegistry internal constructor(
    private val stateRegistry: StateRegistry,
    private val clientState: ClientState,
    private val mutableGlobalState: MutableGlobalState,
    private val userPresence: Boolean,
    private val repos: RepositoryFacade,
    private val client: ChatClient,
    private val coroutineScope: CoroutineScope,
    private val queryingChannelsFree: StateFlow<Boolean>,
) : ChannelStateLogicProvider {

    private val queryChannels: ConcurrentHashMap<Pair<FilterObject, QuerySorter<Channel>>, QueryChannelsLogic> =
        ConcurrentHashMap()
    private val channels: ConcurrentHashMap<Pair<String, String>, ChannelLogic> = ConcurrentHashMap()
    private val threads: ConcurrentHashMap<String, ThreadLogic> = ConcurrentHashMap()

    internal fun queryChannels(filter: FilterObject, sort: QuerySorter<Channel>): QueryChannelsLogic {
        return queryChannels.getOrPut(filter to sort) {
            val queryChannelsStateLogic = QueryChannelsStateLogic(
                stateRegistry.queryChannels(filter, sort).toMutableState(),
                stateRegistry,
                this,
                coroutineScope,
            )

            val queryChannelsDatabaseLogic = QueryChannelsDatabaseLogic(
                queryChannelsRepository = repos,
                channelConfigRepository = repos,
                channelRepository = repos,
                repositoryFacade = repos,
            )

            QueryChannelsLogic(
                filter,
                sort,
                client,
                queryChannelsStateLogic,
                queryChannelsDatabaseLogic,
            )
        }
    }

    internal fun queryChannels(queryChannelsRequest: QueryChannelsRequest): QueryChannelsLogic =
        queryChannels(queryChannelsRequest.filter, queryChannelsRequest.querySort)

    fun channel(channelType: String, channelId: String): ChannelLogic {
        return channels.getOrPut(channelType to channelId) {
            val mutableState = stateRegistry.mutableChannel(channelType, channelId)
            val stateLogic = ChannelStateLogic(
                clientState = clientState,
                mutableState = mutableState,
                globalMutableState = mutableGlobalState,
                searchLogic = SearchLogic(mutableState),
                coroutineScope = coroutineScope,
            )

            ChannelLogic(
                repos = repos,
                userPresence = userPresence,
                channelStateLogic = stateLogic,
                coroutineScope = coroutineScope,
            ) {
                clientState.user.value?.id
            }
        }
    }

    internal fun removeChannel(channelType: String, channelId: String) {
        channels.remove(channelType to channelId)
    }

    fun channelState(channelType: String, channelId: String): ChannelStateLogic {
        return channel(channelType, channelId).stateLogic()
    }

    fun channelFromMessageId(messageId: String): ChannelLogic? {
        return channels.values.find { channelLogic ->
            channelLogic.getMessage(messageId) != null
        }
    }

    fun getMessageById(messageId: String): Message? {
        return channelFromMessageId(messageId)?.getMessage(messageId)
            ?: threadFromMessageId(messageId)?.getMessage(messageId)
    }

    suspend fun getMessageByIdFromDb(messageId: String): Message? = repos.selectMessage(messageId)?.copy()

    fun channelFromMessage(message: Message): ChannelLogic? {
        return if (message.parentId == null || message.showInChannel) {
            val (channelType, channelId) = message.cid.cidToTypeAndId()
            channel(channelType, channelId)
        } else {
            null
        }
    }

    fun threadFromMessageId(messageId: String): ThreadLogic? {
        return threads.values.find { threadLogic ->
            threadLogic.getMessage(messageId) != null
        }
    }

    fun threadFromMessage(message: Message): ThreadLogic? {
        return message.parentId?.let { thread(it) }
    }

    override fun channelStateLogic(channelType: String, channelId: String): ChannelStateLogic {
        return channel(channelType, channelId).stateLogic()
    }

    fun thread(messageId: String): ThreadLogic {
        return threads.getOrPut(messageId) {
            val mutableState = stateRegistry.mutableThread(messageId)
            val stateLogic = ThreadStateLogic(mutableState)
            ThreadLogic(stateLogic).also { threadLogic ->
                coroutineScope.launch {
                    repos.selectMessage(messageId)?.let { threadLogic.upsertMessage(it) }
                    repos.selectMessagesForThread(messageId, MESSAGE_LIMIT).let { threadLogic.upsertMessages(it) }
                }
            }
        }
    }

    fun getActiveQueryChannelsLogic(): List<QueryChannelsLogic> = queryChannels.values.toList()


    fun isActiveChannel(channelType: String, channelId: String): Boolean =
        channels.containsKey(channelType to channelId)

    fun getActiveChannelsLogic(): List<ChannelLogic> = channels.values.toList()

    fun isActiveThread(messageId: String): Boolean =
        threads.containsKey(messageId)

    fun clear() {
        queryChannels.clear()
        channels.clear()
        threads.clear()
        mutableGlobalState.destroy()
    }

    companion object {
        private const val MESSAGE_LIMIT = 30
    }
}
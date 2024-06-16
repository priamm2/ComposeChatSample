package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.QueryChannelsSpec
import com.example.composechatsample.core.QueryChannelsState
import com.example.composechatsample.core.api.QueryChannelsRequest
import com.example.composechatsample.core.events.ChatEvent
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.querysort.QuerySorter
import com.example.composechatsample.core.updateUsers
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class QueryChannelsMutableState(
    override val filter: FilterObject,
    override val sort: QuerySorter<Channel>,
    scope: CoroutineScope,
    latestUsers: StateFlow<Map<String, User>>,
) : QueryChannelsState {

    private val logger by taggedLogger("Chat:QueryChannelsState")

    internal var rawChannels: Map<String, Channel>?
        get() = _channels?.value
        private set(value) {
            _channels?.value = value
        }

    internal val queryChannelsSpec: QueryChannelsSpec = QueryChannelsSpec(filter, sort)

    private var _channels: MutableStateFlow<Map<String, Channel>?>? = MutableStateFlow(null)
    private val mapChannels: StateFlow<Map<String, Channel>?> = _channels!!
    private var _loading: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _loadingMore: MutableStateFlow<Boolean>? = MutableStateFlow(false)

    internal val currentLoading: StateFlow<Boolean>
        get() = if (channels.value.isNullOrEmpty()) loading else loadingMore

    private var _endOfChannels: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private val sortedChannels: StateFlow<List<Channel>?> =
        mapChannels.combine(latestUsers) { channelMap, userMap ->
            channelMap?.values?.updateUsers(userMap)
        }.map { channels ->
            channels?.sortedWith(sort.comparator)
        }.stateIn(scope, SharingStarted.Eagerly, null)
    private var _currentRequest: MutableStateFlow<QueryChannelsRequest?>? = MutableStateFlow(null)
    private var _recoveryNeeded: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _channelsOffset: MutableStateFlow<Int>? = MutableStateFlow(0)
    internal val channelsOffset: StateFlow<Int> = _channelsOffset!!

    override var chatEventHandlerFactory: ChatEventHandlerFactory? = null

    override val recoveryNeeded: StateFlow<Boolean> = _recoveryNeeded!!

    private val eventHandler: ChatEventHandler by lazy {
        (chatEventHandlerFactory ?: ChatEventHandlerFactory()).chatEventHandler(mapChannels)
    }

    fun handleChatEvent(event: ChatEvent, cachedChannel: Channel?): EventHandlingResult {
        return eventHandler.handleChatEvent(event, filter, cachedChannel)
    }

    override val currentRequest: StateFlow<QueryChannelsRequest?> = _currentRequest!!
    override val loading: StateFlow<Boolean> = _loading!!
    override val loadingMore: StateFlow<Boolean> = _loadingMore!!
    override val endOfChannels: StateFlow<Boolean> = _endOfChannels!!
    override val channels: StateFlow<List<Channel>?> = sortedChannels
    override val channelsStateData: StateFlow<ChannelsStateData> =
        loading.combine(sortedChannels) { loading: Boolean, channels: List<Channel>? ->
            when {
                loading || channels == null -> ChannelsStateData.Loading
                channels.isEmpty() -> ChannelsStateData.OfflineNoResults
                else -> ChannelsStateData.Result(channels)
            }
        }.stateIn(scope, SharingStarted.Eagerly, ChannelsStateData.NoQueryActive)

    override val nextPageRequest: StateFlow<QueryChannelsRequest?> =
        currentRequest.combine(channelsOffset) { currentRequest, currentOffset ->
            currentRequest?.copy(offset = currentOffset)
        }.stateIn(scope, SharingStarted.Eagerly, null)


    fun setLoadingMore(isLoading: Boolean) {
        _loadingMore?.value = isLoading
    }

    fun setLoadingFirstPage(isLoading: Boolean) {
        _loading?.value = isLoading
    }

    fun setCurrentRequest(request: QueryChannelsRequest) {
        _currentRequest?.value = request
    }

    fun setEndOfChannels(isEnd: Boolean) {
        _endOfChannels?.value = isEnd
    }

    fun setRecoveryNeeded(recoveryNeeded: Boolean) {
        _recoveryNeeded?.value = recoveryNeeded
    }

    fun setChannelsOffset(offset: Int) {
        _channelsOffset?.value = offset
    }

    fun setChannels(channelsMap: Map<String, Channel>) {
        rawChannels = channelsMap
    }

    fun destroy() {
        _channels = null
        _loading = null
        _loadingMore = null
        _endOfChannels = null
        _currentRequest = null
        _recoveryNeeded = null
        _channelsOffset = null
    }
}
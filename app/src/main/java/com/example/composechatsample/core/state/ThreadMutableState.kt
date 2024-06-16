package com.example.composechatsample.core.state

import com.example.composechatsample.core.models.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ThreadMutableState(
    override val parentId: String,
    scope: CoroutineScope,
) : ThreadState {
    private var _messages: MutableStateFlow<Map<String, Message>>? = MutableStateFlow(emptyMap())
    private var _loading: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _endOfOlderMessages: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _endOfNewerMessages: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _oldestInThread: MutableStateFlow<Message?>? = MutableStateFlow(null)
    private var _newestInThread: MutableStateFlow<Message?>? = MutableStateFlow(null)

    val rawMessage: StateFlow<Map<String, Message>> = _messages!!
    override val messages: StateFlow<List<Message>> = rawMessage
        .map { it.values }
        .map { threadMessages -> threadMessages.sortedBy { m -> m.createdAt ?: m.createdLocallyAt } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())
    override val loading: StateFlow<Boolean> = _loading!!
    override val endOfOlderMessages: StateFlow<Boolean> = _endOfOlderMessages!!
    override val endOfNewerMessages: StateFlow<Boolean> = _endOfNewerMessages!!
    override val oldestInThread: StateFlow<Message?> = _oldestInThread!!
    override val newestInThread: StateFlow<Message?> = _newestInThread!!

    fun setLoading(isLoading: Boolean) {
        _loading?.value = isLoading
    }

    fun setEndOfOlderMessages(isEnd: Boolean) {
        _endOfOlderMessages?.value = isEnd
    }

    fun setOldestInThread(message: Message?) {
        _oldestInThread?.value = message
    }

    fun setEndOfNewerMessages(isEnd: Boolean) {
        _endOfNewerMessages?.value = isEnd
    }

    fun setNewestInThread(message: Message?) {
        _newestInThread?.value = message
    }

    fun deleteMessage(message: Message) {
        _messages?.apply { value -= message.id }
    }

    fun upsertMessages(messages: List<Message>) {
        _messages?.apply { value += messages.associateBy(Message::id) }
    }

    fun destroy() {
        _messages = null
        _loading = null
        _endOfOlderMessages = null
        _oldestInThread = null
    }
}
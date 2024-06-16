package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.events.HasMessage
import com.example.composechatsample.core.events.MessageDeletedEvent
import com.example.composechatsample.core.events.MessageUpdatedEvent
import com.example.composechatsample.core.events.NewMessageEvent
import com.example.composechatsample.core.events.NotificationMessageNewEvent
import com.example.composechatsample.core.events.ReactionDeletedEvent
import com.example.composechatsample.core.events.ReactionNewEvent
import com.example.composechatsample.core.events.ReactionUpdateEvent
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.state.ThreadMutableState
import com.example.composechatsample.core.state.ThreadStateLogic

class ThreadLogic(
    private val threadStateLogic: ThreadStateLogic,
) {

    private val mutableState: ThreadMutableState = threadStateLogic.writeThreadState()

    fun isLoadingMessages(): Boolean = mutableState.loading.value

    internal fun setLoading(isLoading: Boolean) {
        mutableState.setLoading(isLoading)
    }

    internal fun getMessage(messageId: String): Message? {
        return mutableState.rawMessage.value[messageId]?.copy()
    }

    internal fun stateLogic(): ThreadStateLogic {
        return threadStateLogic
    }

    internal fun deleteMessage(message: Message) {
        threadStateLogic.deleteMessage(message)
    }

    internal fun upsertMessage(message: Message) = upsertMessages(listOf(message))

    internal fun upsertMessages(messages: List<Message>) = threadStateLogic.upsertMessages(messages)

    internal fun removeLocalMessage(message: Message) {
        threadStateLogic.deleteMessage(message)
    }

    internal fun setEndOfOlderMessages(isEnd: Boolean) {
        mutableState.setEndOfOlderMessages(isEnd)
    }

    internal fun updateOldestMessageInThread(messages: List<Message>) {
        mutableState.setOldestInThread(
            messages.sortedBy { it.createdAt }
                .firstOrNull()
                ?: mutableState.oldestInThread.value,
        )
    }

    internal fun setEndOfNewerMessages(isEnd: Boolean) {
        mutableState.setEndOfNewerMessages(isEnd)
    }

    internal fun updateNewestMessageInThread(messages: List<Message>) {
        mutableState.setNewestInThread(
            messages.sortedBy { it.createdAt }
                .lastOrNull()
                ?: mutableState.newestInThread.value,
        )
    }

    internal fun handleEvents(events: List<HasMessage>) {
        val messages = events
            .map { event ->
                val ownReactions = getMessage(event.message.id)?.ownReactions ?: event.message.ownReactions
                if (event is MessageUpdatedEvent) {
                    event.message.copy(
                        replyTo = mutableState.messages.value.firstOrNull { it.id == event.message.replyMessageId },
                        ownReactions = ownReactions,
                    )
                } else {
                    event.message.copy(
                        ownReactions = ownReactions,
                    )
                }
            }
        upsertMessages(messages)
    }

    private fun handleEvent(event: HasMessage) {
        when (event) {
            is MessageUpdatedEvent -> {
                event.message.copy(
                    replyTo = mutableState.messages.value.firstOrNull { it.id == event.message.replyMessageId },
                ).let(::upsertMessage)
            }
            is NewMessageEvent,
            is MessageDeletedEvent,
            is NotificationMessageNewEvent,
            is ReactionNewEvent,
            is ReactionUpdateEvent,
            is ReactionDeletedEvent,
            -> {
                upsertMessage(event.message)
            }
            else -> Unit
        }
    }
}
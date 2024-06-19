package com.example.composechatsample.core.state

import com.example.composechatsample.core.models.EventType
import com.example.composechatsample.core.plugin.ChannelMutableState
import com.example.composechatsample.core.plugin.TypingEventListener
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.events.ChatEvent
import java.util.Date

internal class TypingEventListenerState(
    private val state: StateRegistry,
) : TypingEventListener {


    override fun onTypingEventPrecondition(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ): Result<Unit> {
        val channelState = state.mutableChannel(channelType, channelId)
        return when (eventType) {
            EventType.TYPING_START -> {
                onTypingStartPrecondition(channelState, eventTime)
            }
            EventType.TYPING_STOP -> {
                onTypingStopPrecondition(channelState)
            }
            else -> Result.Success(Unit)
        }
    }

    private fun onTypingStopPrecondition(channelState: ChannelMutableState): Result<Unit> {
        return if (!channelState.channelConfig.value.typingEventsEnabled) {
            Result.Failure(Error.GenericError("Typing events are not enabled"))
        } else if (channelState.lastStartTypingEvent == null) {
            Result.Failure(
                Error.GenericError(
                    "lastStartTypingEvent is null. " +
                        "Make sure to send Event.TYPING_START before sending Event.TYPING_STOP",
                ),
            )
        } else {
            Result.Success(Unit)
        }
    }

    private fun onTypingStartPrecondition(channelState: ChannelMutableState, eventTime: Date): Result<Unit> {
        return if (!channelState.channelConfig.value.typingEventsEnabled) {
            Result.Failure(Error.GenericError("Typing events are not enabled"))
        } else if (channelState.lastStartTypingEvent != null &&
            eventTime.time - channelState.lastStartTypingEvent!!.time < TYPING_DELAY
        ) {
            Result.Failure(
                Error.GenericError(
                    "Last typing event was sent at ${channelState.lastStartTypingEvent}. " +
                        "There must be a delay of $TYPING_DELAY_SECS seconds before sending new event",
                ),
            )
        } else {
            Result.Success(Unit)
        }
    }

    override fun onTypingEventRequest(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ) {
        val channelState = state.mutableChannel(channelType, channelId)

        if (eventType == EventType.TYPING_START) {
            channelState.lastStartTypingEvent = eventTime
        } else if (eventType == EventType.TYPING_STOP) {
            channelState.lastStartTypingEvent = null
        }
    }


    override fun onTypingEventResult(
        result: Result<ChatEvent>,
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ) {
        if (result is Result.Success) {
            val channelState = state.mutableChannel(channelType, channelId)

            when (eventType) {
                EventType.TYPING_START ->
                    channelState.keystrokeParentMessageId =
                        extraData[ARG_TYPING_PARENT_ID] as? String
                EventType.TYPING_STOP -> channelState.keystrokeParentMessageId = null
            }
        }
    }

    private companion object {
        private const val ARG_TYPING_PARENT_ID = "parent_id"
        private const val MILLI_SEC = 1000
        private const val TYPING_DELAY_SECS = 3
        private const val TYPING_DELAY = TYPING_DELAY_SECS * MILLI_SEC
    }
}
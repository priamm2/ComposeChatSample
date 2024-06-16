package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.events.ChatEvent
import java.util.Date
import com.example.composechatsample.core.Result

public interface TypingEventListener {

    public fun onTypingEventPrecondition(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ): Result<Unit>

    public fun onTypingEventRequest(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    )

    @Suppress("LongParameterList")
    public fun onTypingEventResult(
        result: Result<ChatEvent>,
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    )
}
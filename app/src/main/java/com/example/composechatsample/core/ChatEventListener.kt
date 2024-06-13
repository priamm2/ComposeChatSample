package com.example.composechatsample.core

import com.example.composechatsample.core.events.ChatEvent

fun interface ChatEventListener<EventT : ChatEvent> {
    fun onEvent(event: EventT)
}
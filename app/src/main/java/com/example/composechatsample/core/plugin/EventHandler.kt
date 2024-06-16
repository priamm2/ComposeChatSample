package com.example.composechatsample.core.plugin

import androidx.annotation.VisibleForTesting
import com.example.composechatsample.core.events.ChatEvent

internal interface EventHandler {

    fun startListening()

    fun stopListening()

    @VisibleForTesting
    suspend fun handleEvents(vararg events: ChatEvent)
}
package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable
import java.util.Date

@Immutable
public data class Config(
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val name: String = "",
    val typingEventsEnabled: Boolean = true,
    val readEventsEnabled: Boolean = true,
    val connectEventsEnabled: Boolean = true,
    val searchEnabled: Boolean = true,
    val isReactionsEnabled: Boolean = true,
    val isThreadEnabled: Boolean = true,
    val muteEnabled: Boolean = true,
    val uploadsEnabled: Boolean = true,
    val urlEnrichmentEnabled: Boolean = true,
    val customEventsEnabled: Boolean = false,
    val pushNotificationsEnabled: Boolean = true,
    val messageRetention: String = "infinite",
    val maxMessageLength: Int = 5000,
    val automod: String = "disabled",
    val automodBehavior: String = "",
    val blocklistBehavior: String = "",
    val commands: List<Command> = mutableListOf(),
)

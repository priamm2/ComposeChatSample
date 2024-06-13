package com.example.composechatsample.core

import com.example.composechatsample.log.ChatLogLevel
import com.example.composechatsample.log.ChatLoggerHandler

public interface ChatLoggerConfig {
    public val level: ChatLogLevel
    public val handler: ChatLoggerHandler?
}

internal data class ChatLoggerConfigImpl(
    override val level: ChatLogLevel,
    override val handler: ChatLoggerHandler?,
) : ChatLoggerConfig
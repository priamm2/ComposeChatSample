package com.example.composechatsample.core

import com.example.composechatsample.core.notifications.NotificationConfig

public class ChatClientConfig @JvmOverloads constructor(
    public val apiKey: String,
    public var httpUrl: String,
    public var cdnHttpUrl: String,
    public var wssUrl: String,
    public val warmUp: Boolean,
    public val loggerConfig: ChatLoggerConfig,
    public var distinctApiCalls: Boolean = true,
    public val debugRequests: Boolean,
    public val notificationConfig: NotificationConfig,
) {
    public var isAnonymous: Boolean = false
}
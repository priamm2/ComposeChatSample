package com.example.composechatsample.core.models

public object ChannelCapabilities {
    public const val BAN_CHANNEL_MEMBERS: String = "ban-channel-members"
    public const val CONNECT_EVENTS: String = "connect-events"
    public const val DELETE_ANY_MESSAGE: String = "delete-any-message"
    public const val DELETE_CHANNEL: String = "delete-channel"
    public const val DELETE_OWN_MESSAGE: String = "delete-own-message"
    public const val FLAG_MESSAGE: String = "flag-message"
    public const val FREEZE_CHANNEL: String = "freeze-channel"
    public const val LEAVE_CHANNEL: String = "leave-channel"
    public const val JOIN_CHANNEL: String = "join-channel"
    public const val MUTE_CHANNEL: String = "mute-channel"
    public const val PIN_MESSAGE: String = "pin-message"
    public const val QUOTE_MESSAGE: String = "quote-message"
    public const val READ_EVENTS: String = "read-events"
    public const val SEARCH_MESSAGES: String = "search-messages"
    public const val SEND_CUSTOM_EVENTS: String = "send-custom-events"
    public const val SEND_LINKS: String = "send-links"
    public const val SEND_MESSAGE: String = "send-message"
    public const val SEND_REACTION: String = "send-reaction"
    public const val SEND_REPLY: String = "send-reply"
    public const val SET_CHANNEL_COOLDOWN: String = "set-channel-cooldown"

    @Deprecated(
        "Use TYPING_EVENTS instead.",
    )
    public const val SEND_TYPING_EVENTS: String = "send-typing-events"
    public const val UPDATE_ANY_MESSAGE: String = "update-any-message"
    public const val UPDATE_CHANNEL: String = "update-channel"
    public const val UPDATE_CHANNEL_MEMBERS: String = "update-channel-members"
    public const val UPDATE_OWN_MESSAGE: String = "update-own-message"
    public const val UPLOAD_FILE: String = "upload-file"
    public const val TYPING_EVENTS: String = "typing-events"
    public const val SLOW_MODE: String = "slow-mode"
    public const val SKIP_SLOW_MODE: String = "skip-slow-mode"
    public const val JOIN_CALL: String = "join-call"
    public const val CREATE_CALL: String = "create-call"
}

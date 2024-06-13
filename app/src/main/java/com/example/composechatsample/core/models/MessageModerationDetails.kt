package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable

@Immutable
public data class MessageModerationDetails(
    val originalText: String,
    val action: MessageModerationAction,
    val errorMsg: String,
)

@Immutable
public data class MessageModerationAction(
    public val rawValue: String,
) {
    public companion object {

        public val bounce: MessageModerationAction = MessageModerationAction(
            rawValue = "MESSAGE_RESPONSE_ACTION_BOUNCE",
        )

        public val flag: MessageModerationAction = MessageModerationAction(
            rawValue = "MESSAGE_RESPONSE_ACTION_BOUNCE",
        )

        public val block: MessageModerationAction = MessageModerationAction(
            rawValue = "MESSAGE_RESPONSE_ACTION_BLOCK",
        )

        public val values: Set<MessageModerationAction> = setOf(bounce, flag, block)

        public fun fromRawValue(rawValue: String): MessageModerationAction = values.find {
            it.rawValue == rawValue
        } ?: MessageModerationAction(rawValue = rawValue)
    }
}

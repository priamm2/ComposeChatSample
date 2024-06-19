package com.example.composechatsample.ui.theme

public enum class MessageOptionsUserReactionAlignment(public val value: Int) {
    START(0),
    END(1),
    BY_USER(2),
    @Suppress("MagicNumber")
    BY_USER_INVERTED(3),
}

public fun Int.getUserReactionAlignment(): MessageOptionsUserReactionAlignment {
    return MessageOptionsUserReactionAlignment.values().firstOrNull { it.value == this } ?: error("No such alignment")
}

public fun MessageOptionsUserReactionAlignment.isStartAlignment(isMine: Boolean): Boolean {
    return this == MessageOptionsUserReactionAlignment.START ||
        (!isMine && this == MessageOptionsUserReactionAlignment.BY_USER) ||
        (isMine && this == MessageOptionsUserReactionAlignment.BY_USER_INVERTED)
}
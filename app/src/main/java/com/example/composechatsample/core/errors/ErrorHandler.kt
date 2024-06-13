package com.example.composechatsample.core.errors

public interface ErrorHandler :
    DeleteReactionErrorHandler,
    CreateChannelErrorHandler,
    QueryMembersErrorHandler,
    SendReactionErrorHandler,
    Comparable<ErrorHandler> {

    public val priority: Int

    override fun compareTo(other: ErrorHandler): Int {
        return this.priority.compareTo(other.priority)
    }

    public companion object {
        public const val DEFAULT_PRIORITY: Int = 1
    }
}
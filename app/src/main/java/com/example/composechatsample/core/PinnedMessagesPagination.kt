package com.example.composechatsample.core

import java.util.Date

public sealed class PinnedMessagesPagination {

    public data class AroundMessage(val messageId: String) : PinnedMessagesPagination()

    public data class BeforeMessage(val messageId: String, val inclusive: Boolean) : PinnedMessagesPagination()

    public data class AfterMessage(val messageId: String, val inclusive: Boolean) : PinnedMessagesPagination()

    public data class AroundDate(val date: Date) : PinnedMessagesPagination()

    public data class BeforeDate(val date: Date, val inclusive: Boolean) : PinnedMessagesPagination()

    public data class AfterDate(val date: Date, val inclusive: Boolean) : PinnedMessagesPagination()
}
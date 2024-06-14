package com.example.composechatsample.core.models

import com.example.composechatsample.core.PinnedMessagesPagination
import com.example.composechatsample.core.models.querysort.QuerySorter

internal data class GetPinnedMessagesHash(
    val channelType: String,
    val channelId: String,
    val limit: Int,
    val sort: QuerySorter<Message>,
    val pagination: PinnedMessagesPagination,
)
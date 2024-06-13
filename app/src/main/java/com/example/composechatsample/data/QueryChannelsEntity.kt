package com.example.composechatsample.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.querysort.QuerySorter

@Entity(tableName = QUERY_CHANNELS_ENTITY_TABLE_NAME)
internal data class QueryChannelsEntity(
    @PrimaryKey
    var id: String,
    val filter: FilterObject,
    val querySort: QuerySorter<Channel>,
    val cids: List<String>,
)

internal const val QUERY_CHANNELS_ENTITY_TABLE_NAME = "stream_channel_query"
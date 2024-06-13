package com.example.composechatsample.core.errors

import com.example.composechatsample.core.Call
import com.example.composechatsample.core.ReturnOnErrorCall
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.querysort.QuerySorter

public interface QueryMembersErrorHandler {


    @Suppress("LongParameterList")
    public fun onQueryMembersError(
        originalCall: Call<List<Member>>,
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ): ReturnOnErrorCall<List<Member>>
}

@Suppress("LongParameterList")
internal fun Call<List<Member>>.onQueryMembersError(
    errorHandlers: List<QueryMembersErrorHandler>,
    channelType: String,
    channelId: String,
    offset: Int,
    limit: Int,
    filter: FilterObject,
    sort: QuerySorter<Member>,
    members: List<Member>,
): Call<List<Member>> {
    return errorHandlers.fold(this) { messageCall, errorHandler ->
        errorHandler.onQueryMembersError(messageCall, channelType, channelId, offset, limit, filter, sort, members)
    }
}
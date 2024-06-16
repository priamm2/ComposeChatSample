
package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.querysort.QuerySorter
import com.example.composechatsample.core.Result

public interface QueryMembersListener {

    @Suppress("LongParameterList")
    public suspend fun onQueryMembersResult(
        result: Result<List<Member>>,
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    )
}

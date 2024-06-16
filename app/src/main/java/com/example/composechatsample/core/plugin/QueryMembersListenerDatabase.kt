package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.querysort.QuerySorter
import com.example.composechatsample.core.repository.ChannelRepository
import com.example.composechatsample.core.repository.UserRepository
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.toCid

internal class QueryMembersListenerDatabase(
    private val userRepository: UserRepository,
    private val channelRepository: ChannelRepository,
) : QueryMembersListener {

    override suspend fun onQueryMembersResult(
        result: Result<List<Member>>,
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ) {
        if (result is Result.Success) {
            val resultMembers = result.value

            userRepository.insertUsers(resultMembers.map(Member::user))
            channelRepository.updateMembersForChannel(Pair(channelType, channelId).toCid(), resultMembers)
        }
    }
}
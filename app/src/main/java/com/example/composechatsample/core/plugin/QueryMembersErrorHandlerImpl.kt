package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.Call
import com.example.composechatsample.core.ReturnOnErrorCall
import com.example.composechatsample.core.errors.QueryMembersErrorHandler
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.querysort.QuerySorter
import com.example.composechatsample.core.onErrorReturn
import com.example.composechatsample.core.repository.ChannelRepository
import com.example.composechatsample.core.state.ClientState
import com.example.composechatsample.core.toCid
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.Result

internal class QueryMembersErrorHandlerImpl(
    private val scope: CoroutineScope,
    private val clientState: ClientState,
    private val channelRepository: ChannelRepository,
) : QueryMembersErrorHandler {

    private val logger by taggedLogger("QueryMembersError")

    override fun onQueryMembersError(
        originalCall: Call<List<Member>>,
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ): ReturnOnErrorCall<List<Member>> {
        return originalCall.onErrorReturn(scope) { originalError ->
            logger.d {
                "An error happened while wuery members. " +
                    "Error message: ${originalError.message}. Full error: $originalCall"
            }

            if (clientState.isOnline) {
                Result.Failure(originalError)
            } else {
                // retrieve from database
                val clampedOffset = offset.coerceAtLeast(0)
                val clampedLimit = limit.coerceAtLeast(0)
                val membersFromDatabase = channelRepository
                    .selectMembersForChannel(Pair(channelType, channelId).toCid())
                    .sortedWith(sort.comparator)
                    .drop(clampedOffset)
                    .let { members ->
                        if (clampedLimit > 0) {
                            members.take(clampedLimit)
                        } else {
                            members
                        }
                    }
                Result.Success(membersFromDatabase)
            }
        }
    }
}
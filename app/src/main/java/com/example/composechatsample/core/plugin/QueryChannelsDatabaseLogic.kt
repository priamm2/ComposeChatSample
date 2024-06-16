package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.QueryChannelsSpec
import com.example.composechatsample.core.api.AnyChannelPaginationRequest
import com.example.composechatsample.core.applyPagination
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.ChannelConfig
import com.example.composechatsample.core.repository.ChannelConfigRepository
import com.example.composechatsample.core.repository.ChannelRepository
import com.example.composechatsample.core.repository.QueryChannelsRepository
import com.example.composechatsample.core.repository.RepositoryFacade

class QueryChannelsDatabaseLogic(
    private val queryChannelsRepository: QueryChannelsRepository,
    private val channelConfigRepository: ChannelConfigRepository,
    private val channelRepository: ChannelRepository,
    private val repositoryFacade: RepositoryFacade,
) {

    internal suspend fun storeStateForChannels(channels: Collection<Channel>) {
        repositoryFacade.storeStateForChannels(channels)
    }

    internal suspend fun fetchChannelsFromCache(
        pagination: AnyChannelPaginationRequest,
        queryChannelsSpec: QueryChannelsSpec?,
    ): List<Channel> {
        val query = queryChannelsSpec?.run {
            queryChannelsRepository.selectBy(queryChannelsSpec.filter, queryChannelsSpec.querySort)
        } ?: return emptyList()

        return repositoryFacade.selectChannels(query.cids.toList(), pagination).applyPagination(pagination)
    }

    internal suspend fun selectChannel(cid: String): Channel? {
        return channelRepository.selectChannel(cid)
    }

    internal suspend fun selectChannels(cids: List<String>): List<Channel> {
        return channelRepository.selectChannels(cids)
    }

    internal suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec) {
        return queryChannelsRepository.insertQueryChannels(queryChannelsSpec)
    }

    internal suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>) {
        return channelConfigRepository.insertChannelConfigs(configs)
    }
}
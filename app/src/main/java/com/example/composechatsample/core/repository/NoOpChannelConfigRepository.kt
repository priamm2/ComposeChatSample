package com.example.composechatsample.core.repository

import com.example.composechatsample.core.models.ChannelConfig

internal object NoOpChannelConfigRepository : ChannelConfigRepository {
    override suspend fun cacheChannelConfigs() { /* No-Op */ }
    override fun selectChannelConfig(channelType: String): ChannelConfig? = null
    override suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>) { /* No-Op */ }
    override suspend fun insertChannelConfig(config: ChannelConfig) { /* No-Op */ }
    override suspend fun clear() { /* No-Op */ }
}
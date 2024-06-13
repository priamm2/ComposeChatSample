package com.example.composechatsample.core.repository

import com.example.composechatsample.core.models.ChannelConfig

public interface ChannelConfigRepository {
    public suspend fun cacheChannelConfigs()
    public fun selectChannelConfig(channelType: String): ChannelConfig?
    public suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>)
    public suspend fun insertChannelConfig(config: ChannelConfig)
    public suspend fun clear()
}
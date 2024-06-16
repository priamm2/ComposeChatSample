package com.example.composechatsample.core.repository

import com.example.composechatsample.core.models.ChannelConfig
import com.example.composechatsample.core.toEntity
import com.example.composechatsample.core.toModel
import com.example.composechatsample.data.ChannelConfigDao
import com.example.composechatsample.data.ChannelConfigEntity
import java.util.Collections

internal class DatabaseChannelConfigRepository(
    private val channelConfigDao: ChannelConfigDao,
) : ChannelConfigRepository {
    private val channelConfigs: MutableMap<String, ChannelConfig> = Collections.synchronizedMap(mutableMapOf())

    override suspend fun cacheChannelConfigs() {
        channelConfigs += channelConfigDao.selectAll().map(ChannelConfigEntity::toModel)
            .associateBy(ChannelConfig::type)
    }

    override fun selectChannelConfig(channelType: String): ChannelConfig? {
        return channelConfigs[channelType]
    }

    override suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>) {
        channelConfigs += configs.associateBy(ChannelConfig::type)
        channelConfigDao.insert(configs.map(ChannelConfig::toEntity))
    }

    override suspend fun insertChannelConfig(config: ChannelConfig) {
        channelConfigs += config.type to config
        channelConfigDao.insert(config.toEntity())
    }

    override suspend fun clear() {
        channelConfigDao.deleteAll()
    }
}
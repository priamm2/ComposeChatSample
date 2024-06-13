package com.example.composechatsample.core.repository

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User

internal object NoOpRepositoryFactory : RepositoryFactory {
    override fun createUserRepository(): UserRepository = NoOpUserRepository
    override fun createChannelConfigRepository(): ChannelConfigRepository = NoOpChannelConfigRepository
    override fun createQueryChannelsRepository(): QueryChannelsRepository = NoOpQueryChannelsRepository
    override fun createSyncStateRepository(): SyncStateRepository = NoOpSyncStateRepository
    override fun createReactionRepository(
        getUser: suspend (userId: String) -> User,
    ): ReactionRepository = NoOpReactionRepository

    override fun createMessageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository = NoOpMessageRepository

    override fun createChannelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository = NoOpChannelRepository

    object Provider : RepositoryFactory.Provider {
        override fun createRepositoryFactory(user: User): RepositoryFactory = NoOpRepositoryFactory
    }
}
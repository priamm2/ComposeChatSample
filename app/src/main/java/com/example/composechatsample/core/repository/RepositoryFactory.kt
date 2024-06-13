package com.example.composechatsample.core.repository

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User

public interface RepositoryFactory {

    public fun createUserRepository(): UserRepository

    public fun createChannelConfigRepository(): ChannelConfigRepository

    public fun createChannelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository

    public fun createQueryChannelsRepository(): QueryChannelsRepository

    public fun createMessageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository

    public fun createReactionRepository(getUser: suspend (userId: String) -> User): ReactionRepository

    public fun createSyncStateRepository(): SyncStateRepository

    public interface Provider {
        public fun createRepositoryFactory(user: User): RepositoryFactory
    }
}
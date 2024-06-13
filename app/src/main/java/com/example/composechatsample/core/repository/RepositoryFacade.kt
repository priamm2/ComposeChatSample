package com.example.composechatsample.core.repository

import androidx.annotation.VisibleForTesting
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.ChannelConfig
import com.example.composechatsample.core.models.Config
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.User
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import java.util.Date

@SuppressWarnings("LongParameterList")
public class RepositoryFacade private constructor(
    private val userRepository: UserRepository,
    private val configsRepository: ChannelConfigRepository,
    private val channelsRepository: ChannelRepository,
    private val queryChannelsRepository: QueryChannelsRepository,
    private val messageRepository: MessageRepository,
    private val reactionsRepository: ReactionRepository,
    private val syncStateRepository: SyncStateRepository,
    private val scope: CoroutineScope,
    private val defaultConfig: Config,
) : UserRepository by userRepository,
    ChannelRepository by channelsRepository,
    ReactionRepository by reactionsRepository,
    MessageRepository by messageRepository,
    ChannelConfigRepository by configsRepository,
    QueryChannelsRepository by queryChannelsRepository,
    SyncStateRepository by syncStateRepository {

    private val logger by taggedLogger("Chat:RepositoryFacade")

    override suspend fun selectChannels(channelCIDs: List<String>): List<Channel> =
        selectChannels(channelCIDs, null)

    public suspend fun selectChannels(
        channelIds: List<String>,
        pagination: AnyChannelPaginationRequest?,
    ): List<Channel> {
        val channels = channelsRepository.selectChannels(channelIds)
        val messagesMap = if (pagination?.isRequestingMoreThanLastMessage() != false) {
            channelIds.map { cid ->
                scope.async { cid to selectMessagesForChannel(cid, pagination) }
            }.awaitAll().toMap()
        } else {
            emptyMap()
        }

        return channels.map { channel ->
            channel.enrichChannel(messagesMap, defaultConfig)
        }
    }

    @VisibleForTesting
    public fun Channel.enrichChannel(messageMap: Map<String, List<Message>>, defaultConfig: Config): Channel = copy(
        config = selectChannelConfig(type)?.config ?: defaultConfig,
        messages = if (messageMap.containsKey(cid)) {
            val fullList = (messageMap[cid] ?: error("Messages must be in the map")) + messages
            fullList.distinctBy(Message::id)
        } else {
            messages
        },
    )

    override suspend fun insertChannel(channel: Channel) {
        insertUsers(channel.let(Channel::users))
        channelsRepository.insertChannel(channel)
    }

    override suspend fun insertChannels(channels: Collection<Channel>) {
        insertUsers(channels.flatMap(Channel::users))
        channelsRepository.insertChannels(channels)
    }

    override suspend fun insertMessage(message: Message) {
        insertUsers(message.users())
        messageRepository.insertMessage(message)
    }

    override suspend fun insertMessages(messages: List<Message>) {
        insertUsers(messages.flatMap(Message::users))
        messageRepository.insertMessages(messages)
    }

    override suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date) {
        messageRepository.deleteChannelMessagesBefore(cid, hideMessagesBefore)
    }

    override suspend fun deleteChannelMessage(message: Message) {
        messageRepository.deleteChannelMessage(message)
        channelsRepository.deleteChannelMessage(message)
    }

    override suspend fun insertReaction(reaction: Reaction) {
        val messageId = reaction.messageId
        if (messageId.isEmpty()) {
            logger.w { "[insertReaction] rejected (message id cannot be empty)" }
            return
        }
        val user = reaction.user
        if (user == null) {
            logger.w { "[insertReaction] rejected (user cannot be null)" }
            return
        }
        if (messageRepository.selectMessage(messageId) == null) {
            logger.w { "[insertReaction] rejected (message cannot be found in local DB)" }
            return
        }
        logger.d { "[insertReaction] reaction: ${reaction.type}, messageId: $messageId" }
        insertUser(user)
        reactionsRepository.insertReaction(reaction)
    }

    override suspend fun updateMembersForChannel(cid: String, members: List<Member>) {
        insertUsers(members.map(Member::user))
        channelsRepository.updateMembersForChannel(cid, members)
    }

    public suspend fun storeStateForChannels(channels: Collection<Channel>) {
        insertChannelConfigs(channels.map { ChannelConfig(it.type, it.config) })
        insertChannels(channels)
        insertMessages(
            channels.flatMap { channel ->
                channel.messages.map { it.enrichWithCid(channel.cid) }
            },
        )
    }

    override suspend fun deleteChannel(cid: String) {
        channelsRepository.deleteChannel(cid)
        messageRepository.deleteChannelMessages(cid)
    }

    public suspend fun storeStateForChannel(channel: Channel) {
        storeStateForChannels(listOf(channel))
    }

    override suspend fun clear() {
        userRepository.clear()
        channelsRepository.clear()
        reactionsRepository.clear()
        messageRepository.clear()
        configsRepository.clear()
        queryChannelsRepository.clear()
        syncStateRepository.clear()
    }

    public companion object {

        public fun create(
            factory: RepositoryFactory,
            scope: CoroutineScope,
            defaultConfig: Config = Config(),
        ): RepositoryFacade {
            val userRepository = factory.createUserRepository()
            val getUser: suspend (userId: String) -> User = { userId ->
                requireNotNull(userRepository.selectUser(userId)) {
                    "User with the userId: `$userId` has not been found"
                }
            }

            val messageRepository = factory.createMessageRepository(getUser)
            val getMessage: suspend (messageId: String) -> Message? = messageRepository::selectMessage

            return RepositoryFacade(
                userRepository = userRepository,
                configsRepository = factory.createChannelConfigRepository(),
                channelsRepository = factory.createChannelRepository(getUser, getMessage),
                queryChannelsRepository = factory.createQueryChannelsRepository(),
                messageRepository = messageRepository,
                reactionsRepository = factory.createReactionRepository(getUser),
                syncStateRepository = factory.createSyncStateRepository(),
                scope = scope,
                defaultConfig = defaultConfig,
            )
        }
    }
}
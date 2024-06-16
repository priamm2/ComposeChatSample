package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.latestOrNull
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.repository.RepositoryFacade
import com.example.composechatsample.core.updateLastMessage
import com.example.composechatsample.core.updateUsers
import com.example.composechatsample.core.users
import com.example.composechatsample.log.StreamLog
import com.example.composechatsample.log.taggedLogger
import java.util.Date

internal class EventBatchUpdate private constructor(
    private val id: Int,
    private val currentUserId: String,
    private val globalState: GlobalState,
    private val repos: RepositoryFacade,
    private val channelMap: MutableMap<String, Channel>,
    private val messageMap: MutableMap<String, Message>,
    private val userMap: MutableMap<String, User>,
) {

    private val logger by taggedLogger(TAG)

    fun addMessageData(receivedEventDate: Date, cid: String, message: Message) {
        addMessage(message)
        getCurrentChannel(cid)
            ?.updateLastMessage(receivedEventDate, message, currentUserId)
            ?.let(::addChannel)
    }

    fun addChannel(channel: Channel) {
        logger.v {
            "[addChannel] id: $id" +
                ", channel.lastMessageAt: ${channel.lastMessageAt}" +
                ", channel.latestMessageId: ${channel.messages.latestOrNull()?.id}"
        }
        addUsers(channel.users())
        channelMap += (channel.cid to channel)
    }

    fun getCurrentChannel(cId: String): Channel? = channelMap[cId]
    fun getCurrentMessage(messageId: String): Message? = messageMap[messageId]

    fun addMessage(message: Message) {
        addUsers(message.users())
        messageMap += (message.id to message)
    }

    fun addUsers(newUsers: List<User>) {
        newUsers.forEach { user ->
            if (userMap.containsKey(user.id).not()) {
                userMap[user.id] = user
            }
        }
    }

    fun addUser(newUser: User) {
        userMap += (newUser.id to newUser)
    }

    suspend fun execute() {
        currentUserId?.let { userMap -= it }
        logger.v { "[execute] id: $id, channelMap.size: ${channelMap.size}" }

        repos.insertUsers(userMap.values.toList())
        repos.insertChannels(channelMap.values.updateUsers(userMap))
        repos.insertMessages(messageMap.values.toList().updateUsers(userMap))
    }

    private suspend fun enrichChannelsWithCapabilities() {
        val channelsWithoutCapabilities = channelMap.values
            .filter { channel -> channel.ownCapabilities.isEmpty() }
            .map { channel -> channel.cid }
        val cachedChannels = repos.selectChannels(channelsWithoutCapabilities)
        logger.v { "[enrichChannelsWithCapabilities] id: $id, cachedChannels.size: ${cachedChannels.size}" }
        channelMap.putAll(cachedChannels.associateBy(Channel::cid))
    }

    internal class Builder(
        private val id: Int,
    ) {
        private val channelsToFetch = mutableSetOf<String>()
        private val channelsToRemove = mutableSetOf<String>()
        private val messagesToFetch = mutableSetOf<String>()
        private val users = mutableSetOf<User>()

        fun addToFetchChannels(cIds: List<String>) {
            channelsToFetch += cIds
        }

        fun addToRemoveChannels(cIds: List<String>) {
            channelsToRemove += cIds
        }

        fun addToFetchChannels(cId: String) {
            channelsToFetch += cId
        }

        fun addToFetchMessages(ids: List<String>) {
            messagesToFetch += ids
        }

        fun addToFetchMessages(id: String) {
            messagesToFetch += id
        }

        fun addUsers(usersToAdd: List<User>) {
            users += usersToAdd
        }

        suspend fun build(
            globalState: GlobalState,
            repos: RepositoryFacade,
            currentUserId: String,
        ): EventBatchUpdate {
            channelsToRemove.forEach { repos.deleteChannel(it) }
            repos.insertUsers(users)
            val messageMap: Map<String, Message> =
                repos.selectMessages(messagesToFetch.toList()).associateBy(Message::id)
            val channelMap: Map<String, Channel> =
                repos.selectChannels(channelsToFetch.toList()).associateBy(Channel::cid)
            StreamLog.v(TAG) {
                "[builder.build] id: $id, messageMap.size: ${messageMap.size}" +
                    ", channelMap.size: ${channelMap.size}"
            }
            return EventBatchUpdate(
                id,
                currentUserId,
                globalState,
                repos,
                channelMap.toMutableMap(),
                messageMap.toMutableMap(),
                users.associateBy(User::id).toMutableMap(),
            )
        }
    }

    private companion object {
        private const val TAG = "Chat:EventBatchUpdate"
    }
}
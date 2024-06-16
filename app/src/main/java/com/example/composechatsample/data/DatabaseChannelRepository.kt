package com.example.composechatsample.data

import androidx.collection.LruCache
import com.example.composechatsample.core.launchWithMutex
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.mapper.syncUnreadCountWithReads
import com.example.composechatsample.core.repository.ChannelRepository
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date

internal class DatabaseChannelRepository(
    private val scope: CoroutineScope,
    private val channelDao: ChannelDao,
    private val getUser: suspend (userId: String) -> User,
    private val getMessage: suspend (messageId: String) -> Message?,
    cacheSize: Int = 1000,
) : ChannelRepository {

    private val logger by taggedLogger("Chat:ChannelRepository")
    private val channelCache = LruCache<String, Channel>(cacheSize)
    private val dbMutex = Mutex()

    override suspend fun insertChannel(channel: Channel) {
        insertChannels(listOf(channel))
    }

    override suspend fun insertChannels(channels: Collection<Channel>) {
        if (channels.isEmpty()) return
        val updatedChannels = channels
            .map { channelCache[it.cid]?.let { cachedChannel -> it.combine(cachedChannel) } ?: it }
        val channelToInsert = updatedChannels
            .filter { channelCache[it.cid] != it }
            .map { it.toEntity() }
        cacheChannel(updatedChannels)
        scope.launchWithMutex(dbMutex) {
            logger.v {
                "[insertChannels] inserting ${channelToInsert.size} entities on DB, " +
                    "updated ${updatedChannels.size} on cache"
            }
            channelToInsert
                .takeUnless { it.isEmpty() }
                ?.let { channelDao.insertMany(it) }
        }
    }

    private fun cacheChannel(vararg channels: Channel) {
        channels.forEach { channelCache.put(it.cid, it) }
    }

    private fun cacheChannel(channels: Collection<Channel>) {
        channels.forEach { channelCache.put(it.cid, it) }
    }

    override suspend fun deleteChannel(cid: String) {
        logger.v { "[deleteChannel] cid: $cid" }
        channelCache.remove(cid)
        scope.launchWithMutex(dbMutex) { channelDao.delete(cid) }
    }

    override suspend fun deleteChannelMessage(message: Message) {
        channelCache[message.cid]?.let { cachedChannel ->
            val updatedChannel = cachedChannel.copy(messages = cachedChannel.messages.filter { it.id != message.id })
            cacheChannel(updatedChannel)
        }
    }

    override suspend fun selectChannel(cid: String): Channel? =
        channelCache[cid] ?: channelDao.select(cid = cid)?.toModel(getUser, getMessage)
            ?.also { cacheChannel(it) }

    override suspend fun selectChannels(cids: List<String>): List<Channel> {
        val cachedChannels = cids.mapNotNull { channelCache[it] }
        val missingChannelIds = cids.minus(cachedChannels.map(Channel::cid).toSet())
        return cachedChannels +
            channelDao.select(missingChannelIds)
                .map { it.toModel(getUser, getMessage) }
                .also { cacheChannel(it) }
    }

    override suspend fun selectAllCids(): List<String> = channelDao.selectAllCids()

    override suspend fun selectChannelCidsBySyncNeeded(limit: Int): List<String> {
        return channelDao.selectCidsBySyncNeeded(limit = limit)
    }

    override suspend fun selectChannelsSyncNeeded(limit: Int): List<Channel> {
        return channelDao.selectSyncNeeded(limit = limit).map { it.toModel(getUser, getMessage) }
    }

    override suspend fun setChannelDeletedAt(cid: String, deletedAt: Date) {
        channelCache[cid]?.let { cachedChannel ->
            cacheChannel(listOf(cachedChannel.copy(deletedAt = deletedAt)))
        }
        scope.launchWithMutex(dbMutex) { channelDao.setDeletedAt(cid, deletedAt) }
    }

    override suspend fun setHiddenForChannel(cid: String, hidden: Boolean, hideMessagesBefore: Date) {
        channelCache[cid]?.let { cachedChannel ->
            cacheChannel(
                cachedChannel.copy(
                    hidden = hidden,
                    hiddenMessagesBefore = hideMessagesBefore,
                ),
            )
        }
        scope.launchWithMutex(dbMutex) { channelDao.setHidden(cid, hidden, hideMessagesBefore) }
    }

    override suspend fun setHiddenForChannel(cid: String, hidden: Boolean) {
        channelCache[cid]?.let { cachedChannel ->
            cacheChannel(listOf(cachedChannel.copy(hidden = hidden)))
        }
        scope.launchWithMutex(dbMutex) { channelDao.setHidden(cid, hidden) }
    }

    override suspend fun selectMembersForChannel(cid: String): List<Member> =
        selectChannel(cid)?.members ?: emptyList()

    override suspend fun updateMembersForChannel(cid: String, members: List<Member>) {
        selectChannel(cid)?.let {
            insertChannel(it.copy(members = (members + it.members).distinctBy(Member::getUserId)))
        }
    }

    override suspend fun updateLastMessageForChannel(cid: String, lastMessage: Message) {
        selectChannel(cid)?.let {
            insertChannel(
                it.copy(
                    messages = listOf(lastMessage),
                    lastMessageAt = it.lastMessageAt
                        .takeIf { lastMessage.parentId != null && !lastMessage.showInChannel }
                        ?: lastMessage.createdAt
                        ?: lastMessage.createdLocallyAt
                        ?: Date(0),
                ),
            )
        }
    }

    private fun Channel.combine(cachedChannel: Channel): Channel {
        val hideMessagesBefore = minOf(this.hiddenMessagesBefore, cachedChannel.hiddenMessagesBefore)
        val messages = (
            messages.filter { it.after(hideMessagesBefore) } +
                cachedChannel.messages.filter { it.after(hideMessagesBefore) }
            )
            .distinctBy { it.id }
            .sortedBy { it.createdAt ?: it.createdLocallyAt ?: Date(0) }
        val read = (read + cachedChannel.read).distinctBy { it.getUserId() }
        return copy(
            messages = messages,
            lastMessageAt = maxOf(
                lastMessageAt,
                cachedChannel.lastMessageAt,
                messages
                    .filterNot { it.parentId != null && !it.showInChannel }
                    .lastOrNull()
                    ?.let { it.createdAt ?: it.createdLocallyAt ?: Date(0) },
            ),
            hiddenMessagesBefore = hideMessagesBefore,
            members = members,
            read = read,
        ).syncUnreadCountWithReads()
    }
    private fun Message.after(date: Date?): Boolean =
        date?.let { (createdAt ?: createdLocallyAt ?: Date(0)).after(it) } ?: true

    override suspend fun evictChannel(cid: String) {
        logger.v { "[evictChannel] cid: $cid" }
        channelCache.remove(cid)
    }

    override suspend fun clear() {
        dbMutex.withLock { channelDao.deleteAll() }
    }
}
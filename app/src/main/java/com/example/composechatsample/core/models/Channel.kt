package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.querysort.ComparableFieldProvider
import java.util.Date

@Immutable
public data class Channel(
    val id: String = "",
    val type: String = "",
    val name: String = "",
    val image: String = "",
    val watcherCount: Int = 0,
    val frozen: Boolean = false,
    val lastMessageAt: Date? = null,
    val createdAt: Date? = null,
    val deletedAt: Date? = null,
    val updatedAt: Date? = null,
    val syncStatus: SyncStatus = SyncStatus.COMPLETED,
    val memberCount: Int = 0,
    val messages: List<Message> = listOf(),
    val members: List<Member> = listOf(),
    val watchers: List<User> = listOf(),
    val read: List<ChannelUserRead> = listOf(),
    val config: Config = Config(),
    val createdBy: User = User(),
    @Deprecated(
        message = "Use the extension property Channel.currentUserUnreadCount instead",
        replaceWith = ReplaceWith(
            expression = "currentUserUnreadCount",
            imports = ["io.getstream.chat.android.client.extensions.currentUserUnreadCount"],
        ),
        level = DeprecationLevel.WARNING,
    )
    val unreadCount: Int = 0,
    val team: String = "",
    val hidden: Boolean? = null,
    val hiddenMessagesBefore: Date? = null,
    val cooldown: Int = 0,
    val pinnedMessages: List<Message> = listOf(),
    val ownCapabilities: Set<String> = setOf(),
    val membership: Member? = null,
    val cachedLatestMessages: List<Message> = emptyList(),
    val isInsideSearch: Boolean = false,
    override val extraData: Map<String, Any> = mapOf(),
) : CustomObject, ComparableFieldProvider {

    val cid: String
        get() = if (id.isEmpty() || type.isEmpty()) {
            ""
        } else {
            "$type:$id"
        }

    val lastUpdated: Date?
        get() = lastMessageAt?.takeIf { createdAt == null || it.after(createdAt) } ?: createdAt

    val hasUnread: Boolean
        get() = unreadCount > 0

    @Suppress("ComplexMethod")
    override fun getComparableField(fieldName: String): Comparable<*>? {
        return when (fieldName) {
            "cid" -> cid
            "id" -> id
            "type" -> type
            "name" -> name
            "image" -> image
            "watcher_count", "watcherCount" -> watcherCount
            "frozen" -> frozen
            "last_message_at", "lastMessageAt" -> lastMessageAt
            "created_at", "createdAt" -> createdAt
            "updated_at", "updatedAt" -> updatedAt
            "deleted_at", "deletedAt" -> deletedAt
            "member_count", "memberCount" -> memberCount
            "team" -> team
            "hidden" -> hidden
            "cooldown" -> cooldown
            "last_updated", "lastUpdated" -> lastUpdated
            "unread_count", "unreadCount" -> unreadCount
            "has_unread", "hasUnread" -> hasUnread
            else -> extraData[fieldName] as? Comparable<*>
        }
    }

    @SinceKotlin("99999.9")
    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public fun newBuilder(): Builder = Builder(this)

    @Suppress("TooManyFunctions")
    public class Builder() {
        private var id: String = ""
        private var type: String = ""
        private var name: String = ""
        private var image: String = ""
        private var watcherCount: Int = 0
        private var frozen: Boolean = false
        private var lastMessageAt: Date? = null
        private var createdAt: Date? = null
        private var deletedAt: Date? = null
        private var updatedAt: Date? = null
        private var syncStatus: SyncStatus = SyncStatus.COMPLETED
        private var memberCount: Int = 0
        private var messages: List<Message> = listOf()
        private var members: List<Member> = listOf()
        private var watchers: List<User> = listOf()
        private var read: List<ChannelUserRead> = listOf()
        private var config: Config = Config()
        private var createdBy: User = User()
        private var unreadCount: Int = 0
        private var team: String = ""
        private var hidden: Boolean? = null
        private var hiddenMessagesBefore: Date? = null
        private var cooldown: Int = 0
        private var pinnedMessages: List<Message> = listOf()
        private var ownCapabilities: Set<String> = setOf()
        private var membership: Member? = null
        private var cachedLatestMessages: List<Message> = emptyList()
        private var isInsideSearch: Boolean = false
        private var extraData: Map<String, Any> = mapOf()

        public constructor(channel: Channel) : this() {
            id = channel.id
            type = channel.type
            name = channel.name
            image = channel.image
            watcherCount = channel.watcherCount
            frozen = channel.frozen
            lastMessageAt = channel.lastMessageAt
            createdAt = channel.createdAt
            deletedAt = channel.deletedAt
            updatedAt = channel.updatedAt
            syncStatus = channel.syncStatus
            memberCount = channel.memberCount
            messages = channel.messages
            members = channel.members
            watchers = channel.watchers
            read = channel.read
            config = channel.config
            createdBy = channel.createdBy
            unreadCount = channel.unreadCount
            team = channel.team
            hidden = channel.hidden
            hiddenMessagesBefore = channel.hiddenMessagesBefore
            cooldown = channel.cooldown
            pinnedMessages = channel.pinnedMessages
            ownCapabilities = channel.ownCapabilities
            membership = channel.membership
            cachedLatestMessages = channel.cachedLatestMessages
            isInsideSearch = channel.isInsideSearch
            extraData = channel.extraData
        }

        public fun withId(id: String): Builder = apply { this.id = id }
        public fun withType(type: String): Builder = apply { this.type = type }
        public fun withName(name: String): Builder = apply { this.name = name }
        public fun withImage(image: String): Builder = apply { this.image = image }
        public fun withWatcherCount(watcherCount: Int): Builder = apply { this.watcherCount = watcherCount }
        public fun withFrozen(frozen: Boolean): Builder = apply { this.frozen = frozen }
        public fun withLastMessageAt(lastMessageAt: Date?): Builder = apply { this.lastMessageAt = lastMessageAt }
        public fun withCreatedAt(createdAt: Date?): Builder = apply { this.createdAt = createdAt }
        public fun withDeletedAt(deletedAt: Date?): Builder = apply { this.deletedAt = deletedAt }
        public fun withUpdatedAt(updatedAt: Date?): Builder = apply { this.updatedAt = updatedAt }
        public fun withSyncStatus(syncStatus: SyncStatus): Builder = apply { this.syncStatus = syncStatus }
        public fun withMemberCount(memberCount: Int): Builder = apply { this.memberCount = memberCount }
        public fun withMessages(messages: List<Message>): Builder = apply { this.messages = messages }
        public fun withMembers(members: List<Member>): Builder = apply { this.members = members }
        public fun withWatchers(watchers: List<User>): Builder = apply { this.watchers = watchers }
        public fun withRead(read: List<ChannelUserRead>): Builder = apply { this.read = read }
        public fun withConfig(config: Config): Builder = apply { this.config = config }
        public fun withCreatedBy(createdBy: User): Builder = apply { this.createdBy = createdBy }
        public fun withUnreadCount(unreadCount: Int): Builder = apply { this.unreadCount = unreadCount }
        public fun withTeam(team: String): Builder = apply { this.team = team }
        public fun withHidden(hidden: Boolean?): Builder = apply { this.hidden = hidden }
        public fun withHiddenMessagesBefore(hiddenMessagesBefore: Date?): Builder = apply {
            this.hiddenMessagesBefore = hiddenMessagesBefore
        }
        public fun withCooldown(cooldown: Int): Builder = apply { this.cooldown = cooldown }
        public fun withPinnedMessages(pinnedMessages: List<Message>): Builder = apply {
            this.pinnedMessages = pinnedMessages
        }
        public fun withOwnCapabilities(ownCapabilities: Set<String>): Builder = apply {
            this.ownCapabilities = ownCapabilities
        }
        public fun withMembership(membership: Member?): Builder = apply { this.membership = membership }
        public fun withCachedLatestMessages(cachedLatestMessages: List<Message>): Builder = apply {
            this.cachedLatestMessages = cachedLatestMessages
        }
        public fun withIsInsideSearch(isInsideSearch: Boolean): Builder = apply { this.isInsideSearch = isInsideSearch }
        public fun withExtraData(extraData: Map<String, Any>): Builder = apply { this.extraData = extraData }

        public fun build(): Channel = Channel(
            id = id,
            type = type,
            name = name,
            image = image,
            watcherCount = watcherCount,
            frozen = frozen,
            lastMessageAt = lastMessageAt,
            createdAt = createdAt,
            deletedAt = deletedAt,
            updatedAt = updatedAt,
            syncStatus = syncStatus,
            memberCount = memberCount,
            messages = messages,
            members = members,
            watchers = watchers,
            read = read,
            config = config,
            createdBy = createdBy,
            unreadCount = unreadCount,
            team = team,
            hidden = hidden,
            hiddenMessagesBefore = hiddenMessagesBefore,
            cooldown = cooldown,
            pinnedMessages = pinnedMessages,
            ownCapabilities = ownCapabilities,
            membership = membership,
            cachedLatestMessages = cachedLatestMessages,
            isInsideSearch = isInsideSearch,
            extraData = extraData,
        )
    }
}

fun Channel.mergeChannelFromEvent(that: Channel): Channel {
    return copy(
        name = that.name,
        image = that.image,
        hidden = that.hidden,
        frozen = that.frozen,
        team = that.team,
        config = that.config,
        extraData = that.extraData,
        syncStatus = that.syncStatus,
        hiddenMessagesBefore = that.hiddenMessagesBefore,
        memberCount = that.memberCount,
        members = that.members,
        lastMessageAt = when (that.lastMessageAt?.after(lastMessageAt)) {
            true -> that.lastMessageAt
            else -> this.lastMessageAt
        },
        createdAt = that.createdAt,
        updatedAt = that.updatedAt,
        deletedAt = that.deletedAt,
    )
}

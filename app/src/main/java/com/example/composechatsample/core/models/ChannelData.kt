package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable
import java.util.Date

@Immutable
public data class ChannelData(
    val id: String,
    val type: String,
    val name: String = "",
    val image: String = "",
    val createdBy: User = User(),
    val cooldown: Int = 0,
    val frozen: Boolean = false,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val deletedAt: Date? = null,
    val memberCount: Int = 0,
    val team: String = "",
    val extraData: Map<String, Any> = mapOf(),
    val ownCapabilities: Set<String> = setOf(),
    val membership: Member? = null,
) {

    val cid: String
        get() = if (id.isEmpty() || type.isEmpty()) {
            ""
        } else {
            "$type:$id"
        }

    public constructor(channel: Channel, currentOwnCapabilities: Set<String>) : this(
        type = channel.type,
        id = channel.id,
        name = channel.name,
        image = channel.image,
        frozen = channel.frozen,
        cooldown = channel.cooldown,
        createdAt = channel.createdAt,
        updatedAt = channel.updatedAt,
        deletedAt = channel.deletedAt,
        memberCount = channel.memberCount,
        extraData = channel.extraData,
        createdBy = channel.createdBy,
        team = channel.team,
        ownCapabilities = channel.ownCapabilities.takeIf { ownCapabilities -> ownCapabilities.isNotEmpty() }
            ?: currentOwnCapabilities,
        membership = channel.membership,
    )

    @Suppress("LongParameterList")
    public fun toChannel(
        messages: List<Message>,
        cachedLatestMessages: List<Message>,
        members: List<Member>,
        reads: List<ChannelUserRead>,
        watchers: List<User>,
        watcherCount: Int,
        insideSearch: Boolean,
    ): Channel {
        val messagesList = if (insideSearch) cachedLatestMessages else messages
        return Channel(
            type = type,
            id = id,
            name = name,
            image = image,
            frozen = frozen,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            extraData = extraData,
            cooldown = cooldown,
            lastMessageAt = messagesList
                .filterNot { it.shadowed }
                .filterNot { it.parentId != null && !it.showInChannel }
                .lastOrNull()
                ?.let { it.createdAt ?: it.createdLocallyAt },
            createdBy = createdBy,
            messages = messages,
            members = members,
            watchers = watchers,
            watcherCount = watcherCount,
            read = reads,
            team = team,
            memberCount = memberCount,
            ownCapabilities = ownCapabilities,
            membership = membership,
            cachedLatestMessages = cachedLatestMessages,
            isInsideSearch = insideSearch,
        )
    }

    public fun isUserAbleTo(channelCapability: String): Boolean {
        return ownCapabilities.contains(channelCapability)
    }
}

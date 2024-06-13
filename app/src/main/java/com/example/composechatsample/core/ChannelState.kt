package com.example.composechatsample.core;

import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.ChannelData
import com.example.composechatsample.core.models.ChannelUserRead
import com.example.composechatsample.core.models.Config
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.MessagesState
import com.example.composechatsample.core.models.TypingEvent
import com.example.composechatsample.core.models.User
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

public interface ChannelState {
    public val channelType: String
    public val channelId: String
    public val cid: String
    public val repliedMessage: StateFlow<Message?>
    public val quotedMessagesMap: StateFlow<Map<String, List<String>>>
    public val messages: StateFlow<List<Message>>
    public val messagesState: StateFlow<MessagesState>
    public val oldMessages: StateFlow<List<Message>>
    public val watcherCount: StateFlow<Int>
    public val watchers: StateFlow<List<User>>
    public val typing: StateFlow<TypingEvent>
    public val reads: StateFlow<List<ChannelUserRead>>
    public val read: StateFlow<ChannelUserRead?>
    public val unreadCount: StateFlow<Int>
    public val members: StateFlow<List<Member>>
    public val membersCount: StateFlow<Int>
    public val channelData: StateFlow<ChannelData>
    public val hidden: StateFlow<Boolean>
    public val muted: StateFlow<Boolean>
    public val loading: StateFlow<Boolean>
    public val loadingOlderMessages: StateFlow<Boolean>
    public val loadingNewerMessages: StateFlow<Boolean>
    public val endOfOlderMessages: StateFlow<Boolean>
    public val endOfNewerMessages: StateFlow<Boolean>
    public val recoveryNeeded: Boolean
    public val channelConfig: StateFlow<Config>
    public val insideSearch: StateFlow<Boolean>
    public val lastSentMessageDate: StateFlow<Date?>
    public fun toChannel(): Channel
    public fun getMessageById(id: String): Message?
}
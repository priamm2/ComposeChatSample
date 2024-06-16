package com.example.composechatsample.core.plugin;

import com.example.composechatsample.core.models.ChannelMute
import com.example.composechatsample.core.models.Mute
import com.example.composechatsample.core.models.TypingEvent
import kotlinx.coroutines.flow.StateFlow

public interface GlobalState {
    public val totalUnreadCount: StateFlow<Int>
    public val channelUnreadCount: StateFlow<Int>
    public val muted: StateFlow<List<Mute>>
    public val channelMutes: StateFlow<List<ChannelMute>>
    public val banned: StateFlow<Boolean>
    public val typingChannels: StateFlow<Map<String, TypingEvent>>
}
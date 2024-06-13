package com.example.composechatsample.core.notifications

import com.example.composechatsample.core.events.NewMessageEvent
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.PushMessage

public interface NotificationHandler {

    public fun onChatEvent(event: NewMessageEvent): Boolean {
        return true
    }

    public fun onPushMessage(message: PushMessage): Boolean {
        return false
    }

    public fun showNotification(channel: Channel, message: Message)

    public fun dismissChannelNotifications(channelType: String, channelId: String)

    public fun dismissAllNotifications()

    public fun onNotificationPermissionStatus(status: NotificationPermissionStatus)
}
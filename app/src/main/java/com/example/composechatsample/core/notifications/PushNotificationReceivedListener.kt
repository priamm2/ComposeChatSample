package com.example.composechatsample.core.notifications

public fun interface PushNotificationReceivedListener {
    public fun onPushNotificationReceived(channelType: String, channelId: String)
}
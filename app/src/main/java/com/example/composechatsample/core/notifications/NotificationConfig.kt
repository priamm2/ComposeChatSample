package com.example.composechatsample.core.notifications

data class NotificationConfig @JvmOverloads constructor(
    val pushNotificationsEnabled: Boolean = true,
    val ignorePushMessagesWhenUserOnline: Boolean = true,
    val pushDeviceGenerators: List<PushDeviceGenerator> = listOf(),
    val shouldShowNotificationOnPush: () -> Boolean = { true },
    val requestPermissionOnAppLaunch: () -> Boolean = { true },
    val autoTranslationEnabled: Boolean = false,
)
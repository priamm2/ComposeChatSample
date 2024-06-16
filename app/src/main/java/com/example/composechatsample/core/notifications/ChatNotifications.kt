package com.example.composechatsample.core.notifications

import android.app.Application
import android.content.Context
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.DevicePushProvider
import com.example.composechatsample.core.DispatcherProvider
import com.example.composechatsample.core.LoadNotificationDataWorker
import com.example.composechatsample.core.events.NewMessageEvent
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Device
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.PushMessage
import com.example.composechatsample.core.models.PushProvider
import com.example.composechatsample.core.push.PushDevice
import com.example.composechatsample.core.toDevicePushProvider
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal interface ChatNotifications {
    fun onSetUser()
    fun setDevice(device: Device)
    fun onPushMessage(message: PushMessage, pushNotificationReceivedListener: PushNotificationReceivedListener)
    fun onNewMessageEvent(newMessageEvent: NewMessageEvent)
    suspend fun onLogout(flushPersistence: Boolean)
    fun displayNotification(channel: Channel, message: Message)
    fun dismissChannelNotifications(channelType: String, channelId: String)
}

@Suppress("TooManyFunctions")
internal class ChatNotificationsImpl constructor(
    private val handler: NotificationHandler,
    private val notificationConfig: NotificationConfig,
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(DispatcherProvider.IO),
) : ChatNotifications {
    private val logger by taggedLogger("Chat:Notifications")

    private val pushTokenUpdateHandler = PushTokenUpdateHandler(context)
    private val showedMessages = mutableSetOf<String>()
    private val permissionManager: NotificationPermissionManager =
        NotificationPermissionManager.createNotificationPermissionsManager(
            application = context.applicationContext as Application,
            requestPermissionOnAppLaunch = notificationConfig.requestPermissionOnAppLaunch,
            onPermissionStatus = { status ->
                logger.i { "[onPermissionStatus] status: $status" }
                handler.onNotificationPermissionStatus(status)
            },
        )

    init {
        logger.i { "<init> no args" }
    }

    override fun onSetUser() {
        logger.i { "[onSetUser] no args" }
        permissionManager
            .takeIf { notificationConfig.requestPermissionOnAppLaunch() }
            ?.start()
        notificationConfig.pushDeviceGenerators.firstOrNull { it.isValidForThisDevice(context) }
            ?.let {
                it.onPushDeviceGeneratorSelected()
                it.asyncGenerateDevice {
                    setDevice(
                        Device(
                            token = it.token,
                            pushProvider = it.pushProvider.toDevicePushProvider(),
                            providerName = it.providerName,
                            )
                    )
                }
            }
    }

    override fun setDevice(device: Device) {
        logger.i { "[setDevice] device: $device" }
        scope.launch {
            pushTokenUpdateHandler.updateDeviceIfNecessary(device)
        }
    }

    override fun onPushMessage(
        message: PushMessage,
        pushNotificationReceivedListener: PushNotificationReceivedListener,
    ) {
        logger.i { "[onReceivePushMessage] message: $message" }

        pushNotificationReceivedListener.onPushNotificationReceived(message.channelType, message.channelId)

        if (notificationConfig.shouldShowNotificationOnPush() && !handler.onPushMessage(message)) {
            handlePushMessage(message)
        }
    }

    override fun onNewMessageEvent(newMessageEvent: NewMessageEvent) {
        val currentUserId = ChatClient.instance().getCurrentUser()?.id
        if (newMessageEvent.message.user.id == currentUserId) return

        logger.d { "[onNewMessageEvent] event: $newMessageEvent" }
        if (!handler.onChatEvent(newMessageEvent)) {
            logger.i { "[onNewMessageEvent] handle event internally" }
            handleEvent(newMessageEvent)
        }
    }

    override suspend fun onLogout(flushPersistence: Boolean) {
        logger.i { "[onLogout] flusPersistence: $flushPersistence" }
        permissionManager.stop()
        handler.dismissAllNotifications()
        cancelLoadDataWork()
        if (flushPersistence) { removeStoredDevice() }
    }

    private fun cancelLoadDataWork() {
        LoadNotificationDataWorker.cancel(context)
    }

    override fun dismissChannelNotifications(channelType: String, channelId: String) {
        handler.dismissChannelNotifications(channelType, channelId)
    }

    private fun handlePushMessage(message: PushMessage) {
        obtainNotificationData(message.channelId, message.channelType, message.messageId)
    }

    private fun obtainNotificationData(channelId: String, channelType: String, messageId: String) {
        logger.d { "[obtainNotificationData] channelCid: $channelId:$channelType, messageId: $messageId" }
        LoadNotificationDataWorker.start(
            context = context,
            channelId = channelId,
            channelType = channelType,
            messageId = messageId,
        )
    }

    private fun handleEvent(event: NewMessageEvent) {
        obtainNotificationData(event.channelId, event.channelType, event.message.id)
    }

    private fun wasNotificationDisplayed(messageId: String) = showedMessages.contains(messageId)

    override fun displayNotification(channel: Channel, message: Message) {
        logger.d { "[displayNotification] channel.cid: ${channel.cid}, message.cid: ${message.cid}" }
        if (!wasNotificationDisplayed(message.id)) {
            showedMessages.add(message.id)
            handler.showNotification(channel, message)
        }
    }

    private suspend fun removeStoredDevice() {
        pushTokenUpdateHandler.removeStoredDevice()
    }
}

internal object NoOpChatNotifications : ChatNotifications {
    override fun onSetUser() = Unit
    override fun setDevice(device: Device) = Unit
    override fun onPushMessage(
        message: PushMessage,
        pushNotificationReceivedListener: PushNotificationReceivedListener,
    ) = Unit

    override fun onNewMessageEvent(newMessageEvent: NewMessageEvent) = Unit
    override suspend fun onLogout(flushPersistence: Boolean) = Unit
    override fun displayNotification(channel: Channel, message: Message) = Unit
    override fun dismissChannelNotifications(channelType: String, channelId: String) = Unit
}
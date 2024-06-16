package com.example.composechatsample.core.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.composechatsample.R
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User

internal class ChatNotificationHandler(
    private val context: Context,
    private val newMessageIntent: (message: Message, channel: Channel) -> Intent,
    private val notificationChannel: (() -> NotificationChannel),
    private val autoTranslationEnabled: Boolean = false,
) : NotificationHandler {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(
            SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE,
        )
    }
    private val notificationManager: NotificationManager by lazy {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.createNotificationChannel(notificationChannel())
            }
        }
    }

    private fun getNotificationChannelId(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel().id
        } else {
            ""
        }
    }

    override fun onNotificationPermissionStatus(status: NotificationPermissionStatus) { /* no-op */ }

    override fun showNotification(channel: Channel, message: Message) {
        val notificationId: Int = System.nanoTime().toInt()
        val notificationSummaryId = getNotificationGroupSummaryId(channel.type, channel.id)
        addNotificationId(notificationId, notificationSummaryId)
        showNotification(notificationId, buildNotification(notificationId, channel, message).build())
        showNotification(notificationSummaryId, buildNotificationGroupSummary(channel, message).build())
    }

    private fun buildNotification(
        notificationId: Int,
        channel: Channel,
        message: Message,
    ): NotificationCompat.Builder {
        val currentUser = ChatClient.instance().getCurrentUser()
            ?: ChatClient.instance().getStoredUser()
        val displayedText = when (autoTranslationEnabled) {
            true -> currentUser?.language?.let { userLanguage ->
                message.getTranslation(userLanguage).ifEmpty { message.text }
            } ?: message.text
            else -> message.text
        }
        return getNotificationBuilder(
            contentTitle = channel.getNotificationContentTitle(),
            contentText = displayedText,
            groupKey = getNotificationGroupKey(channelType = channel.type, channelId = channel.id),
            intent = getNewMessageIntent(message = message, channel = channel),
        ).apply {
            addAction(NotificationMessageReceiver.createReadAction(context, notificationId, channel, message))
            addAction(NotificationMessageReceiver.createReplyAction(context, notificationId, channel))
            setDeleteIntent(NotificationMessageReceiver.createDismissPendingIntent(context, notificationId, channel))
        }
    }

    private fun buildNotificationGroupSummary(channel: Channel, message: Message): NotificationCompat.Builder {
        return getNotificationBuilder(
            contentTitle = channel.getNotificationContentTitle(),
            contentText = context.getString(R.string.stream_chat_notification_group_summary_content_text),
            groupKey = getNotificationGroupKey(channelType = channel.type, channelId = channel.id),
            intent = getNewMessageIntent(message = message, channel = channel),
        ).apply {
            setGroupSummary(true)
        }
    }

    private fun getNotificationGroupKey(channelType: String, channelId: String): String {
        return "$channelType:$channelId"
    }

    private fun getNotificationGroupSummaryId(channelType: String, channelId: String): Int {
        return getNotificationGroupKey(channelType = channelType, channelId = channelId).hashCode()
    }

    private fun getRequestCode(): Int {
        return System.currentTimeMillis().toInt()
    }

    private fun getNewMessageIntent(message: Message, channel: Channel): Intent = newMessageIntent(message, channel)

    override fun dismissChannelNotifications(channelType: String, channelId: String) {
        dismissSummaryNotification(getNotificationGroupSummaryId(channelType, channelId))
    }

    override fun dismissAllNotifications() {
        getNotificationSummaryIds().forEach(::dismissSummaryNotification)
    }

    private fun showNotification(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }

    private fun getNotificationBuilder(
        contentTitle: String,
        contentText: String,
        groupKey: String,
        intent: Intent,
    ): NotificationCompat.Builder {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            getRequestCode(),
            intent,
            flags,
        )

        return NotificationCompat.Builder(context, getNotificationChannelId())
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.stream_ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.stream_ic_notification))
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setShowWhen(true)
            .setContentIntent(contentIntent)
            .setGroup(groupKey)
    }

    private fun Channel.getNotificationContentTitle(): String =
        name.takeIf { it.isNotEmpty() }
            ?: getMemberNamesWithoutCurrentUser()
            ?: context.getString(R.string.stream_chat_notification_title)

    private fun Channel.getMemberNamesWithoutCurrentUser(): String? = getUsersExcludingCurrent()
        .joinToString { it.name }
        .takeIf { it.isNotEmpty() }

    fun Channel.getUsersExcludingCurrent(
        currentUser: User? = ChatClient.instance().getCurrentUser(),
    ): List<User> = getMembersExcludingCurrent(currentUser).map { it.user }

    fun Channel.getMembersExcludingCurrent(
        currentUser: User? = ChatClient.instance().getCurrentUser(),
    ): List<Member> =
        members.filter { it.user.id != currentUser?.id }

    private fun dismissSummaryNotification(notificationSummaryId: Int) {
        getAssociatedNotificationIds(notificationSummaryId).forEach {
            notificationManager.cancel(it)
            removeNotificationId(it)
        }
        notificationManager.cancel(notificationSummaryId)
        sharedPreferences.edit { remove(getNotificationSummaryIdKey(notificationSummaryId)) }
    }

    private fun addNotificationId(notificationId: Int, notificationSummaryId: Int) {
        sharedPreferences.edit {
            putInt(getNotificationIdKey(notificationId), notificationSummaryId)
            putStringSet(
                KEY_NOTIFICATION_SUMMARY_IDS,
                (getNotificationSummaryIds() + notificationSummaryId).map(Int::toString).toSet(),
            )
            putStringSet(
                getNotificationSummaryIdKey(notificationSummaryId),
                (getAssociatedNotificationIds(notificationSummaryId) + notificationId).map(Int::toString).toSet(),
            )
        }
    }

    private fun removeNotificationId(notificationId: Int) {
        sharedPreferences.edit {
            val notificationSummaryId = getAssociatedNotificationSummaryId(notificationId)
            remove(getNotificationIdKey(notificationId))
            putStringSet(
                getNotificationSummaryIdKey(notificationSummaryId),
                (getAssociatedNotificationIds(notificationSummaryId) - notificationId).map(Int::toString).toSet(),
            )
        }
    }

    private fun getNotificationSummaryIds(): Set<Int> =
        sharedPreferences.getStringSet(KEY_NOTIFICATION_SUMMARY_IDS, null).orEmpty().map(String::toInt).toSet()

    private fun getAssociatedNotificationSummaryId(notificationId: Int): Int =
        sharedPreferences.getInt(getNotificationIdKey(notificationId), 0)

    private fun getAssociatedNotificationIds(notificationSummaryId: Int): Set<Int> =
        sharedPreferences.getStringSet(getNotificationSummaryIdKey(notificationSummaryId), null).orEmpty()
            .map(String::toInt).toSet()

    private fun getNotificationIdKey(notificationId: Int) = KEY_PREFIX_NOTIFICATION_ID + notificationId
    private fun getNotificationSummaryIdKey(notificationSummaryId: Int) =
        KEY_PREFIX_NOTIFICATION_SUMMARY_ID + notificationSummaryId

    private companion object {
        private const val SHARED_PREFERENCES_NAME = "stream_notifications.sp"
        private const val KEY_PREFIX_NOTIFICATION_ID = "nId-"
        private const val KEY_PREFIX_NOTIFICATION_SUMMARY_ID = "nSId-"
        private const val KEY_NOTIFICATION_SUMMARY_IDS = "notification_summary_ids"
    }
}
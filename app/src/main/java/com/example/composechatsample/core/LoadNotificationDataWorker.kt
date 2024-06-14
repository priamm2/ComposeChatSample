package com.example.composechatsample.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.composechatsample.R
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.api.QueryChannelRequest
import com.example.composechatsample.log.taggedLogger

internal class LoadNotificationDataWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private val logger by taggedLogger("Chat:Notifications-Loader")

    override suspend fun doWork(): Result {
        val channelId: String = inputData.getString(DATA_CHANNEL_ID)!!
        val channelType: String = inputData.getString(DATA_CHANNEL_TYPE)!!
        val messageId: String = inputData.getString(DATA_MESSAGE_ID)!!

        setForeground(createForegroundInfo())

        return try {
            val client: ChatClient = ChatClient.instance()
            val getMessage = client.getMessage(messageId)
            val getChannel = client.queryChannel(
                channelType,
                channelId,
                QueryChannelRequest().apply {
                    isNotificationUpdate = true
                },
            )

            val result = getChannel.zipWith(getMessage).await()
            when (result) {
                is com.example.composechatsample.core.Result.Success -> {
                    val (channel, message) = result.value
                    val messageParentId = message.parentId

                    if (messageParentId != null) {
                        logger.v { "[doWork] fetching thread parent message." }
                        client.getMessage(messageParentId).await()
                    }
                    ChatClient.displayNotification(channel = channel, message = message)
                    Result.success()
                }
                is com.example.composechatsample.core.Result.Failure -> {
                    logger.e { "Error while loading notification data: ${result.value}" }
                    Result.failure()
                }
            }
        } catch (exception: IllegalStateException) {
            logger.e { "Error while loading notification data: ${exception.message}" }
            Result.failure()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val foregroundNotification = createForegroundNotification(
            notificationChannelId = context.getString(R.string.stream_chat_other_notifications_channel_id),
            notificationChannelName = context.getString(R.string.stream_chat_other_notifications_channel_name),
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                NOTIFICATION_ID,
                foregroundNotification,
                FOREGROUND_SERVICE_TYPE_SHORT_SERVICE,
            )
        } else {
            ForegroundInfo(
                NOTIFICATION_ID,
                foregroundNotification,
            )
        }
    }

    private fun createForegroundNotification(
        notificationChannelId: String,
        notificationChannelName: String,
    ): Notification {
        createSyncNotificationChannel(notificationChannelId, notificationChannelName)
        return NotificationCompat.Builder(context, notificationChannelId)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.stream_ic_notification)
            .setContentTitle(context.getString(R.string.stream_chat_load_notification_data_title))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createSyncNotificationChannel(
        notificationChannelId: String,
        notificationChannelName: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                notificationChannelId,
                notificationChannelName,
                NotificationManager.IMPORTANCE_LOW,
            ).run {
                context.getSystemService(NotificationManager::class.java).createNotificationChannel(this)
            }
        }
    }

    internal companion object {
        private const val DATA_CHANNEL_TYPE = "DATA_CHANNEL_TYPE"
        private const val DATA_CHANNEL_ID = "DATA_CHANNEL_ID"
        private const val DATA_MESSAGE_ID = "DATA_MESSAGE_ID"

        private const val NOTIFICATION_ID = 1
        private const val LOAD_NOTIFICATION_DATA_WORK_NAME = "LOAD_NOTIFICATION_DATA_WORK_NAME"

        fun start(
            context: Context,
            channelId: String,
            channelType: String,
            messageId: String,
        ) {
            val syncMessagesWork = OneTimeWorkRequestBuilder<LoadNotificationDataWorker>()
                .setInputData(
                    workDataOf(
                        DATA_CHANNEL_ID to channelId,
                        DATA_CHANNEL_TYPE to channelType,
                        DATA_MESSAGE_ID to messageId,
                    ),
                )
                .build()

            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    LOAD_NOTIFICATION_DATA_WORK_NAME,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    syncMessagesWork,
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(LOAD_NOTIFICATION_DATA_WORK_NAME)
        }
    }
}
package com.example.composechatsample.core

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.composechatsample.core.models.UploadAttachmentsNetworkType
import com.example.composechatsample.log.streamLog
import com.example.composechatsample.log.taggedLogger
import java.util.UUID

internal class UploadAttachmentsAndroidWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val channelType: String = inputData.getString(DATA_CHANNEL_TYPE)!!
        val channelId: String = inputData.getString(DATA_CHANNEL_ID)!!
        val messageId = inputData.getString(DATA_MESSAGE_ID)!!

        val logger by taggedLogger(TAG)
        val chatClient = ChatClient.instance()
        val repositoryFacade = chatClient.repositoryFacade

        return UploadAttachmentsWorker(
            channelType = channelType,
            channelId = channelId,
            channelStateLogic = chatClient.logicRegistry?.channelStateLogic(channelType, channelId),
            messageRepository = repositoryFacade,
            chatClient = chatClient,
        ).uploadAttachmentsForMessage(
            messageId,
        ).let { result ->
            when (result) {
                is com.example.composechatsample.core.Result.Success -> {
                    logger.d { "[doWork] #uploader; attachments uploaded successfully" }
                    Result.success()
                }
                is com.example.composechatsample.core.Result.Failure -> {
                    logger.i { "[doWork] #uploader; error while uploading attachments: ${result.value}" }
                    Result.failure(Data.Builder().putAll(mapOf(ERROR_KEY to result.value.message)).build())
                }
            }
        }
    }

    companion object {
        private const val TAG = "Chat:SystemUploadWorker"
        private const val DATA_MESSAGE_ID = "message_id"
        private const val DATA_CHANNEL_TYPE = "channel_type"
        private const val DATA_CHANNEL_ID = "channel_id"
        private const val ERROR_KEY = "error"

        fun start(
            context: Context,
            channelType: String,
            channelId: String,
            messageId: String,
            networkType: UploadAttachmentsNetworkType,
        ): UUID {
            val uploadAttachmentsWorkRequest = OneTimeWorkRequestBuilder<UploadAttachmentsAndroidWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(networkType.toNetworkType()).build())
                .setInputData(
                    workDataOf(
                        DATA_CHANNEL_ID to channelId,
                        DATA_CHANNEL_TYPE to channelType,
                        DATA_MESSAGE_ID to messageId,
                    ),
                )
                .build()

            streamLog(tag = TAG) {
                "[start] #uploader; Enqueueing attachments upload work for $messageId"
            }

            WorkManager.getInstance(context).enqueueUniqueWork(
                "$channelId$messageId",
                ExistingWorkPolicy.KEEP,
                uploadAttachmentsWorkRequest,
            )
            return uploadAttachmentsWorkRequest.id
        }

        fun stop(context: Context, workId: UUID) {
            streamLog(tag = TAG) {
                "[stop] #uploader; upload attachments work cancelled"
            }
            WorkManager.getInstance(context).cancelWorkById(workId)
        }

        private fun UploadAttachmentsNetworkType.toNetworkType(): NetworkType = when (this) {
            UploadAttachmentsNetworkType.CONNECTED -> NetworkType.CONNECTED
            UploadAttachmentsNetworkType.UNMETERED -> NetworkType.UNMETERED
            UploadAttachmentsNetworkType.NOT_ROAMING -> NetworkType.NOT_ROAMING
            UploadAttachmentsNetworkType.METERED -> NetworkType.METERED
        }
    }
}
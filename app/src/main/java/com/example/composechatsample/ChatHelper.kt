package com.example.composechatsample

import android.content.Context
import android.util.Log
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.data.UserCredentials
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.InitializationState
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.UploadAttachmentsNetworkType
import com.example.composechatsample.core.notifications.NotificationConfig
import com.example.composechatsample.core.notifications.NotificationHandlerFactory
import com.example.composechatsample.core.plugin.StatePluginConfig
import com.example.composechatsample.core.plugin.StreamOfflinePluginFactory
import com.example.composechatsample.core.state.StreamStatePluginFactory
import com.example.composechatsample.core.push.FirebasePushDeviceGenerator
import com.example.composechatsample.log.ChatLogLevel
import kotlinx.coroutines.flow.transformWhile
import com.example.composechatsample.core.Error

object ChatHelper {

    private const val TAG = "ChatHelper"

    fun initializeSdk(context: Context, apiKey: String) {
        Log.d(TAG, "[init] apiKey: $apiKey")
        val notificationConfig = NotificationConfig(
            pushDeviceGenerators = listOf(FirebasePushDeviceGenerator(providerName = "Firebase")),
            autoTranslationEnabled = ChatApp.autoTranslationEnabled,
        )
        val notificationHandler = NotificationHandlerFactory.createNotificationHandler(
            context = context,
            notificationConfig = notificationConfig,
            newMessageIntent = { message: Message, channel: Channel ->
                MainActivity.createIntent(
                    context = context,
                    channelId = "${channel.type}:${channel.id}",
                    messageId = message.id,
                    parentMessageId = message.parentId,
                )
            },
        )

        val offlinePlugin = StreamOfflinePluginFactory(context)

        val statePluginFactory = StreamStatePluginFactory(
            config = StatePluginConfig(
                backgroundSyncEnabled = true,
                userPresence = true,
            ),
            appContext = context,
        )

        val logLevel = ChatLogLevel.ALL

        ChatClient.Builder(apiKey, context)
            .notifications(notificationConfig, notificationHandler)
            .withPlugins(offlinePlugin, statePluginFactory)
            .logLevel(logLevel)
            .uploadAttachmentsNetworkType(UploadAttachmentsNetworkType.NOT_ROAMING)
            .build()
    }

    suspend fun connectUser(
        userCredentials: UserCredentials,
        onSuccess: () -> Unit = {},
        onError: (Error) -> Unit = {},
    ) {
        ChatClient.instance().run {
            clientState.initializationState
                .transformWhile {
                    emit(it)
                    it != InitializationState.COMPLETE
                }
                .collect {
                    if (it == InitializationState.NOT_INITIALIZED) {
                        connectUser(userCredentials.user, userCredentials.token)
                            .enqueue { result ->
                                result.onError(onError)
                                    .onSuccess {
                                        ChatApp.credentialsRepository.saveUserCredentials(userCredentials)
                                        onSuccess()
                                    }
                            }
                    }
                }
        }
    }

    suspend fun disconnectUser() {
        ChatApp.credentialsRepository.clearCredentials()

        ChatClient.instance().disconnect(false).await()
    }
}
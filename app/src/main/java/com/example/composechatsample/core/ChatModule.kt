package com.example.composechatsample.core

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.example.composechatsample.core.notifications.NotificationHandler
import com.example.composechatsample.core.token.TokenManager
import okhttp3.OkHttpClient

internal class ChatModule(
    appContext: Context,
    clientScope: ClientScope,
    userScope: UserScope,
    config: ChatClientConfig,
    notificationsHandler: NotificationHandler,
    uploader: FileUploader?,
    tokenManager: TokenManager,
    customOkHttpClient: OkHttpClient?,
    clientDebugger: ChatClientDebugger?,
    lifecycle: Lifecycle,
) : BaseChatModule(
    appContext,
    clientScope,
    userScope,
    config,
    notificationsHandler,
    uploader,
    tokenManager,
    customOkHttpClient,
    clientDebugger,
    lifecycle,
) {

    override fun clientBuilder(
        timeout: Long,
        config: ChatClientConfig,
        parser: ChatParser,
        isAnonymousApi: Boolean,
    ): OkHttpClient.Builder {
        return super.clientBuilder(
            timeout,
            config,
            parser,
            isAnonymousApi,
        ).addNetworkInterceptor(flipperInterceptor())
    }
}
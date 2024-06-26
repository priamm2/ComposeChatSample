package com.example.composechatsample.core

import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.Lifecycle
import com.example.composechatsample.core.api.AnonymousApi
import com.example.composechatsample.core.api.AuthenticatedApi
import com.example.composechatsample.core.notifications.ChatNotifications
import com.example.composechatsample.core.notifications.ChatNotificationsImpl
import com.example.composechatsample.core.notifications.NoOpChatNotifications
import com.example.composechatsample.core.notifications.NotificationConfig
import com.example.composechatsample.core.notifications.NotificationHandler
import com.example.composechatsample.core.state.MutableClientState
import com.example.composechatsample.core.state.NetworkStateProvider
import com.example.composechatsample.core.token.TokenManager
import com.example.composechatsample.core.token.TokenManagerImpl
import com.example.composechatsample.core.user.UserStateService
import com.example.composechatsample.log.ChatLogLevel
import com.example.composechatsample.log.StreamLog
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

internal open class BaseChatModule(
    private val appContext: Context,
    private val clientScope: ClientScope,
    private val userScope: UserScope,
    private val config: ChatClientConfig,
    private val notificationsHandler: NotificationHandler,
    private val fileUploader: FileUploader? = null,
    private val tokenManager: TokenManager = TokenManagerImpl(),
    private val customOkHttpClient: OkHttpClient? = null,
    private val clientDebugger: ChatClientDebugger? = null,
    private val lifecycle: Lifecycle,
    private val httpClientConfig: (OkHttpClient.Builder) -> OkHttpClient.Builder = { it },
) {

    private val moshiParser: ChatParser by lazy { MoshiChatParser() }
    private val socketFactory: SocketFactory by lazy { SocketFactory(moshiParser, tokenManager) }

    private val defaultNotifications by lazy { buildNotification(notificationsHandler, config.notificationConfig) }
    private val defaultApi by lazy { buildApi(config) }
    internal val chatSocket: ChatSocket by lazy { buildChatSocket(config) }
    private val defaultFileUploader by lazy {
        StreamFileUploader(buildRetrofitCdnApi())
    }

    val lifecycleObserver: StreamLifecycleObserver by lazy { StreamLifecycleObserver(userScope, lifecycle) }
    val networkStateProvider: NetworkStateProvider by lazy {
        NetworkStateProvider(userScope, appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
    }
    val userStateService: UserStateService = UserStateService()

    val mutableClientState by lazy {
        MutableClientState(networkStateProvider)
    }

    val currentUserFetcher by lazy {
        CurrentUserFetcher(
            networkStateProvider = networkStateProvider,
            socketFactory = socketFactory,
            config = config,
        )
    }


    fun api(): ChatApi = defaultApi

    fun notifications(): ChatNotifications = defaultNotifications


    private fun buildNotification(
        handler: NotificationHandler,
        notificationConfig: NotificationConfig,
    ): ChatNotifications = if (notificationConfig.pushNotificationsEnabled) {
        ChatNotificationsImpl(handler, notificationConfig, appContext)
    } else {
        NoOpChatNotifications
    }

    private fun buildRetrofit(
        endpoint: String,
        timeout: Long,
        config: ChatClientConfig,
        parser: ChatParser,
        isAnonymousApi: Boolean,
    ): Retrofit {
        val okHttpClient = clientBuilder(timeout, config, parser, isAnonymousApi).build()

        return Retrofit.Builder()
            .baseUrl(endpoint)
            .client(okHttpClient)
            .also(parser::configRetrofit)
            .addCallAdapterFactory(RetrofitCallAdapterFactory.create(parser, userScope))
            .build()
    }

    private val baseClient: OkHttpClient by lazy { customOkHttpClient ?: OkHttpClient() }
    private fun baseClientBuilder(): OkHttpClient.Builder =
        baseClient.newBuilder().followRedirects(false)

    protected open fun clientBuilder(
        timeout: Long,
        config: ChatClientConfig,
        parser: ChatParser,
        isAnonymousApi: Boolean,
    ): OkHttpClient.Builder {
        return baseClientBuilder()
            .apply {
                if (baseClient != customOkHttpClient) {
                    connectTimeout(timeout, TimeUnit.MILLISECONDS)
                    writeTimeout(timeout, TimeUnit.MILLISECONDS)
                    readTimeout(timeout, TimeUnit.MILLISECONDS)
                }
            }
            .addInterceptor(ApiKeyInterceptor(config.apiKey))
            .addInterceptor(HeadersInterceptor(getAnonymousProvider(config, isAnonymousApi)))
            .apply {
                if (config.debugRequests) {
                    addInterceptor(ApiRequestAnalyserInterceptor(ApiRequestsAnalyser.get()))
                }
            }
            .let(httpClientConfig)
            .addInterceptor(
                TokenAuthInterceptor(
                    tokenManager,
                    parser,
                    getAnonymousProvider(config, isAnonymousApi),
                ),
            )
            .apply {
                if (config.loggerConfig.level != ChatLogLevel.NOTHING) {
                    addInterceptor(HttpLoggingInterceptor())
                    addInterceptor(
                        CurlInterceptor(
                            logger = object : Logger {
                                override fun log(message: String) {
                                    StreamLog.i("Chat:CURL") { message }
                                }
                            },
                        ),
                    )
                }
            }
            .addNetworkInterceptor(ProgressInterceptor())
    }

    private fun getAnonymousProvider(
        config: ChatClientConfig,
        isAnonymousApi: Boolean,
    ): () -> Boolean {
        return { isAnonymousApi || config.isAnonymous }
    }

    private fun buildChatSocket(
        chatConfig: ChatClientConfig,
    ) = ChatSocket(
        chatConfig.apiKey,
        chatConfig.wssUrl,
        tokenManager,
        socketFactory,
        userScope,
        lifecycleObserver,
        networkStateProvider,
        clientDebugger,
    )

    @Suppress("RemoveExplicitTypeArguments")
    private fun buildApi(chatConfig: ChatClientConfig): ChatApi = MoshiChatApi(
        fileUploader ?: defaultFileUploader,
        buildRetrofitApi<UserApi>(),
        buildRetrofitApi<GuestApi>(),
        buildRetrofitApi<MessageApi>(),
        buildRetrofitApi<ChannelApi>(),
        buildRetrofitApi<DeviceApi>(),
        buildRetrofitApi<ModerationApi>(),
        buildRetrofitApi<GeneralApi>(),
        buildRetrofitApi<ConfigApi>(),
        buildRetrofitApi<VideoCallApi>(),
        buildRetrofitApi<FileDownloadApi>(),
        buildRetrofitApi<OpenGraphApi>(),
        userScope,
        userScope,
    ).let { originalApi ->
        DistinctChatApiEnabler(DistinctChatApi(userScope, originalApi)) {
            chatConfig.distinctApiCalls
        }
    }.let { originalApi ->
        ExtraDataValidator(userScope, originalApi)
    }

    private inline fun <reified T> buildRetrofitApi(): T {
        val apiClass = T::class.java
        return buildRetrofit(
            config.httpUrl,
            BASE_TIMEOUT,
            config,
            moshiParser,
            apiClass.isAnonymousApi,
        ).create(apiClass)
    }

    private val Class<*>.isAnonymousApi: Boolean
        get() {
            val anon = this.annotations.any { it is AnonymousApi }
            val auth = this.annotations.any { it is AuthenticatedApi }

            if (anon && auth) {
                throw IllegalStateException(
                    "Api class must be annotated with either @AnonymousApi or @AuthenticatedApi, and not both",
                )
            }

            if (anon) return true
            if (auth) return false

            throw IllegalStateException("Api class must be annotated with either @AnonymousApi or @AuthenticatedApi")
        }

    private fun buildRetrofitCdnApi(): RetrofitCdnApi {
        val apiClass = RetrofitCdnApi::class.java
        return buildRetrofit(
            config.cdnHttpUrl,
            CDN_TIMEOUT,
            config,
            moshiParser,
            apiClass.isAnonymousApi,
        ).create(apiClass)
    }

    private companion object {
        private const val BASE_TIMEOUT = 30_000L
        private var CDN_TIMEOUT = 30_000L
    }
}
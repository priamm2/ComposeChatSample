package com.example.composechatsample.core

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import androidx.annotation.CheckResult
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.composechatsample.core.errors.ErrorHandler
import com.example.composechatsample.core.errors.StreamChannelNotFoundException
import com.example.composechatsample.core.errors.onCreateChannelError
import com.example.composechatsample.core.errors.onMessageError
import com.example.composechatsample.core.errors.onQueryMembersError
import com.example.composechatsample.core.errors.onReactionError
import com.example.composechatsample.core.events.ChatEvent
import com.example.composechatsample.core.events.ConnectedEvent
import com.example.composechatsample.core.events.ConnectingEvent
import com.example.composechatsample.core.events.DisconnectedEvent
import com.example.composechatsample.core.events.HasOwnUser
import com.example.composechatsample.core.events.NewMessageEvent
import com.example.composechatsample.core.events.NotificationChannelMutesUpdatedEvent
import com.example.composechatsample.core.events.NotificationMutesUpdatedEvent
import com.example.composechatsample.core.events.UserEvent
import com.example.composechatsample.core.events.UserUpdatedEvent
import com.example.composechatsample.core.models.AppSettings
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.BannedUser
import com.example.composechatsample.core.models.BannedUsersSort
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.ConnectionData
import com.example.composechatsample.core.models.ConnectionState
import com.example.composechatsample.core.models.Device
import com.example.composechatsample.core.models.EventType
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.Filters
import com.example.composechatsample.core.models.Flag
import com.example.composechatsample.core.models.GuestUser
import com.example.composechatsample.core.models.InitializationState
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Mute
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.models.querysort.QuerySortByField
import com.example.composechatsample.log.StreamLog
import com.example.composechatsample.log.taggedLogger
import com.example.composechatsample.core.models.PushMessage
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.SearchMessagesResult
import com.example.composechatsample.core.models.UploadAttachmentsNetworkType
import com.example.composechatsample.core.models.UploadedFile
import com.example.composechatsample.core.models.VideoCallInfo
import com.example.composechatsample.core.models.VideoCallToken
import com.example.composechatsample.core.models.querysort.QuerySorter
import com.example.composechatsample.core.notifications.ChatNotifications
import com.example.composechatsample.core.notifications.NotificationConfig
import com.example.composechatsample.core.notifications.NotificationHandler
import com.example.composechatsample.core.notifications.NotificationHandlerFactory
import com.example.composechatsample.core.notifications.PushNotificationReceivedListener
import com.example.composechatsample.core.plugin.DependencyResolver
import com.example.composechatsample.core.plugin.Plugin
import com.example.composechatsample.core.plugin.PluginFactory
import com.example.composechatsample.core.repository.NoOpRepositoryFactory
import com.example.composechatsample.core.repository.RepositoryFacade
import com.example.composechatsample.core.repository.RepositoryFactory
import com.example.composechatsample.core.state.ClientState
import com.example.composechatsample.core.state.MutableClientState
import com.example.composechatsample.core.token.CacheableTokenProvider
import com.example.composechatsample.core.token.ConstantTokenProvider
import com.example.composechatsample.core.token.TokenManager
import com.example.composechatsample.core.token.TokenManagerImpl
import com.example.composechatsample.core.token.TokenProvider
import com.example.composechatsample.core.token.TokenUtils
import com.example.composechatsample.core.user.CredentialConfig
import com.example.composechatsample.core.user.SharedPreferencesCredentialStorage
import com.example.composechatsample.core.user.UserCredentialStorage
import com.example.composechatsample.core.user.UserStateService
import com.example.composechatsample.log.ChatLogLevel
import com.example.composechatsample.log.ChatLoggerHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.full.isSubclassOf
import kotlin.time.Duration.Companion.days

public class ChatClient
@Suppress("LongParameterList")
internal constructor(
    public val config: ChatClientConfig,
    private val api: ChatApi,
    private val notifications: ChatNotifications,
    private val tokenManager: TokenManager = TokenManagerImpl(),
    private val userCredentialStorage: UserCredentialStorage,
    private val userStateService: UserStateService = UserStateService(),
    private val clientDebugger: ChatClientDebugger = StubChatClientDebugger,
    private val tokenUtils: TokenUtils = TokenUtils,
    private val clientScope: ClientScope,
    private val userScope: UserScope,
    internal val retryPolicy: RetryPolicy,
    private val appSettingsManager: AppSettingManager,
    private val chatSocket: ChatSocket,
    public val pluginFactories: List<PluginFactory>,
    private val mutableClientState: MutableClientState,
    private val currentUserFetcher: CurrentUserFetcher,
    private val repositoryFactoryProvider: RepositoryFactory.Provider,
    public val audioPlayer: AudioPlayer,
) {
    private val logger by taggedLogger(TAG)
    private val waitConnection = MutableSharedFlow<Result<ConnectionData>>()
    public val clientState: ClientState = mutableClientState

    private val streamDateFormatter: StreamDateFormatter = StreamDateFormatter()
    private val eventsObservable = ChatEventsObservable(waitConnection, userScope, chatSocket)
    private val eventMutex = Mutex()

    private val initializedUserId = AtomicReference<String?>(null)

    internal fun launch(
        block: suspend CoroutineScope.() -> Unit,
    ) = userScope.launch(block = block)

    public fun inheritScope(block: (Job) -> CoroutineContext): CoroutineScope {
        if (userScope.userId.value == null) {
            logger.e { "[inheritScope] userId is null" }
            clientDebugger.onNonFatalErrorOccurred(
                tag = TAG,
                src = "inheritScope",
                desc = "ChatClient::connectUser() must be called before inheriting scope",
                error = Error.GenericError("userScope.userId.value is null"),
            )
        }
        return userScope + block(userScope.coroutineContext.job)
    }

    public val repositoryFacade: RepositoryFacade
        get() = _repositoryFacade
            ?: (getCurrentUser() ?: getStoredUser())
                ?.let { user ->
                    createRepositoryFacade(userScope, createRepositoryFactory(user))
                        .also { _repositoryFacade = it }
                }
            ?: createRepositoryFacade(userScope)

    private var _repositoryFacade: RepositoryFacade? = null

    private var pushNotificationReceivedListener: PushNotificationReceivedListener =
        PushNotificationReceivedListener { _, _ -> }

    public var plugins: List<Plugin> = emptyList()

    @Throws(IllegalStateException::class)
    @Suppress("ThrowsCount")
    public inline fun <reified DR : DependencyResolver, reified T : Any> resolveDependency(): T {
        StreamLog.d(TAG) { "[resolveDependency] DR: ${DR::class.simpleName}, T: ${T::class.simpleName}" }
        return when {
            DR::class.isSubclassOf(PluginFactory::class) -> resolveFactoryDependency<DR, T>()
            DR::class.isSubclassOf(Plugin::class) -> resolvePluginDependency<DR, T>()
            else -> error("Unsupported dependency resolver: ${DR::class}")
        }
    }

    @PublishedApi
    @Throws(IllegalStateException::class)
    @Suppress("ThrowsCount")
    internal inline fun <reified F : DependencyResolver, reified T : Any> resolveFactoryDependency(): T {
        StreamLog.v(TAG) { "[resolveFactoryDependency] F: ${F::class.simpleName}, T: ${T::class.simpleName}" }
        val resolver = pluginFactories.find { plugin ->
            plugin is F
        } ?: throw IllegalStateException(
            "Factory '${F::class.qualifiedName}' was not found. Did you init it within ChatClient?",
        )
        return resolver.resolveDependency(T::class)
            ?: throw IllegalStateException(
                "Dependency '${T::class.qualifiedName}' was not resolved by factory '${F::class.qualifiedName}'",
            )
    }

    @PublishedApi
    @Throws(IllegalStateException::class)
    @Suppress("ThrowsCount")
    internal inline fun <reified P : DependencyResolver, reified T : Any> resolvePluginDependency(): T {
        StreamLog.v(TAG) { "[resolvePluginDependency] P: ${P::class.simpleName}, T: ${T::class.simpleName}" }
        val initState = clientState.initializationState.value
        if (initState != InitializationState.COMPLETE) {
            StreamLog.e(TAG) { "[resolvePluginDependency] failed (initializationState is not COMPLETE): $initState " }
            throw IllegalStateException("ChatClient::connectUser() must be called before resolving any dependency")
        }
        val resolver = plugins.find { plugin ->
            plugin is P
        } ?: throw IllegalStateException(
            "Plugin '${P::class.qualifiedName}' was not found. Did you init it within ChatClient?",
        )
        return resolver.resolveDependency(T::class)
            ?: throw IllegalStateException(
                "Dependency '${T::class.qualifiedName}' was not resolved by plugin '${P::class.qualifiedName}'",
            )
    }

    private val errorHandlers: List<ErrorHandler>
        get() = plugins.mapNotNull { it.errorHandler }.sorted()

    public var logicRegistry: ChannelStateLogicProvider? = null

    internal lateinit var attachmentsSender: AttachmentsSender

    init {
        eventsObservable.subscribeSuspend { event ->
            eventMutex.withLock {
                handleEvent(event)
            }
        }
        logger.i { "Initialised: ${buildSdkTrackingHeaders()}" }
    }

    private suspend fun handleEvent(event: ChatEvent) {
        when (event) {
            is ConnectedEvent -> {
                logger.i { "[handleEvent] event: ConnectedEvent(userId='${event.me.id}')" }
                val user = event.me
                val connectionId = event.connectionId
                api.setConnection(user.id, connectionId)
                notifications.onSetUser()

                mutableClientState.setConnectionState(ConnectionState.Connected)
                mutableClientState.setUser(user)
            }

            is NewMessageEvent -> {
                notifications.onNewMessageEvent(event)
            }

            is ConnectingEvent -> {
                logger.i { "[handleEvent] event: ConnectingEvent" }
                mutableClientState.setConnectionState(ConnectionState.Connecting)
            }

            is UserUpdatedEvent -> {
                val eventUser = event.user
                val currentUser = clientState.user.value
                if (currentUser?.id == eventUser.id) {
                    val mergedUser = currentUser.mergePartially(eventUser)
                    mutableClientState.setUser(mergedUser)
                }
            }

            is NotificationMutesUpdatedEvent -> {
                mutableClientState.setUser(event.me)
            }

            is NotificationChannelMutesUpdatedEvent -> {
                mutableClientState.setUser(event.me)
            }

            is DisconnectedEvent -> {
                logger.i { "[handleEvent] event: DisconnectedEvent(disconnectCause=${event.disconnectCause})" }
                api.releaseConnection()
                mutableClientState.setConnectionState(ConnectionState.Offline)
                when (event.disconnectCause) {
                    is DisconnectCause.ConnectionReleased,
                    is DisconnectCause.NetworkNotAvailable,
                    is DisconnectCause.WebSocketNotAvailable,
                    is DisconnectCause.Error,
                    -> {
                    }

                    is DisconnectCause.UnrecoverableError -> {
                        disconnectSuspend(true)
                    }
                }
            }

            else -> Unit
        }

        event.extractCurrentUser()?.let { currentUser ->
            userStateService.onUserUpdated(currentUser)
            mutableClientState.setUser(currentUser)
            storePushNotificationsConfig(
                currentUser.id,
                currentUser.name,
                userStateService.state !is UserState.UserSet,
            )
        }
    }

    private fun ChatEvent.extractCurrentUser(): User? {
        return when (this) {
            is HasOwnUser -> me
            is UserEvent -> getCurrentUser()
                ?.takeIf { it.id == user.id }
                ?.mergePartially(user)

            else -> null
        }
    }

    @Suppress("LongMethod")
    private suspend fun setUser(
        user: User,
        tokenProvider: TokenProvider,
        timeoutMilliseconds: Long?,
    ): Result<ConnectionData> {
        val isAnonymous = user == anonUser
        val cacheableTokenProvider = CacheableTokenProvider(tokenProvider)
        val userState = userStateService.state

        return when {
            tokenUtils.getUserId(cacheableTokenProvider.loadToken()) != user.id -> {
                logger.e {
                    "The user_id provided on the JWT token doesn't match with the current user you try to connect"
                }
                Result.Failure(
                    Error.GenericError(
                        "The user_id provided on the JWT token doesn't match with the current user you try to connect",
                    ),
                )
            }

            userState is UserState.NotSet -> {
                logger.v { "[setUser] user is NotSet" }
                mutableClientState.setUser(user)
                initializeClientWithUser(user, cacheableTokenProvider, isAnonymous)
                userStateService.onSetUser(user, isAnonymous)
                chatSocket.connectUser(user, isAnonymous)
                mutableClientState.setInitializationState(InitializationState.COMPLETE)
                waitFirstConnection(timeoutMilliseconds)
            }

            userState is UserState.UserSet -> {
                logger.w {
                    "[setUser] Trying to set user without disconnecting the previous one - " +
                        "make sure that previously set user is disconnected."
                }
                when {
                    userState.user.id != user.id -> {
                        logger.e { "[setUser] Trying to set different user without disconnect previous one." }
                        Result.Failure(
                            Error.GenericError(
                                "User cannot be set until the previous one is disconnected.",
                            ),
                        )
                    }

                    else -> {
                        getConnectionId()?.let {
                            mutableClientState.setInitializationState(InitializationState.COMPLETE)
                            Result.Success(ConnectionData(userState.user, it))
                        }
                            ?: run {
                                logger.e {
                                    "[setUser] Trying to connect the same user twice without a previously completed " +
                                        "connection."
                                }
                                Result.Failure(
                                    Error.GenericError(
                                        "Failed to connect user. Please check you haven't connected a user already.",
                                    ),
                                )
                            }
                    }
                }
            }

            else -> {
                logger.e { "[setUser] Failed to connect user. Please check you don't have connected user already." }
                Result.Failure(
                    Error.GenericError(
                        "Failed to connect user. Please check you don't have connected user already.",
                    ),
                )
            }
        }.onErrorSuspend {
            disconnectSuspend(flushPersistence = true)
        }
    }

    @Synchronized
    private fun initializeClientWithUser(
        user: User,
        tokenProvider: CacheableTokenProvider,
        isAnonymous: Boolean,
    ) {
        logger.i { "[initializeClientWithUser] user.id: '${user.id}'" }
        val clientJobCount = clientScope.coroutineContext[Job]?.children?.count() ?: -1
        val userJobCount = userScope.coroutineContext[Job]?.children?.count() ?: -1
        logger.v { "[initializeClientWithUser] clientJobCount: $clientJobCount, userJobCount: $userJobCount" }
        if (initializedUserId.get() != user.id) {
            _repositoryFacade = createRepositoryFacade(userScope, createRepositoryFactory(user))
            plugins = pluginFactories.map { it.get(user) }
            initializedUserId.set(user.id)
        } else {
            logger.i {
                "[initializeClientWithUser] initializing client with the same user id." +
                    " Skipping repository and plugins recreation"
            }
        }
        plugins.forEach { it.onUserSet(user) }
        config.isAnonymous = isAnonymous
        tokenManager.setTokenProvider(tokenProvider)
        appSettingsManager.loadAppSettings()
        warmUp()
        logger.i { "[initializeClientWithUser] user.id: '${user.id}'completed" }
    }

    private fun createRepositoryFactory(user: User): RepositoryFactory =
        repositoryFactoryProvider.createRepositoryFactory(user)

    private fun createRepositoryFacade(
        scope: CoroutineScope,
        repositoryFactory: RepositoryFactory = NoOpRepositoryFactory,
    ): RepositoryFacade =
        RepositoryFacade.create(repositoryFactory, scope)

    @CheckResult
    public fun appSettings(): Call<AppSettings> = api.appSettings()

    @CheckResult
    @JvmOverloads
    public fun connectUser(
        user: User,
        tokenProvider: TokenProvider,
        timeoutMilliseconds: Long? = null,
    ): Call<ConnectionData> {
        return CoroutineCall(clientScope) {
            userScope.userId.value = user.id
            connectUserSuspend(user, tokenProvider, timeoutMilliseconds)
        }
    }

    private suspend fun connectUserSuspend(
        user: User,
        tokenProvider: TokenProvider,
        timeoutMilliseconds: Long?,
    ): Result<ConnectionData> {
        mutableClientState.setInitializationState(InitializationState.INITIALIZING)
        logger.d { "[connectUserSuspend] userId: '${user.id}', username: '${user.name}'" }
        return setUser(user, tokenProvider, timeoutMilliseconds).also { result ->
            logger.v {
                "[connectUserSuspend] " +
                    "completed: ${result.stringify { "ConnectionData(connectionId=${it.connectionId})" }}"
            }
        }
    }

    @CheckResult
    @JvmOverloads
    public fun switchUser(
        user: User,
        tokenProvider: TokenProvider,
        timeoutMilliseconds: Long? = null,
        onDisconnectionComplete: () -> Unit = {},
    ): Call<ConnectionData> {
        return CoroutineCall(clientScope) {
            logger.d { "[switchUser] user.id: '${user.id}'" }
            userScope.userId.value = user.id
            disconnectUserSuspend(flushPersistence = true)
            onDisconnectionComplete()
            connectUserSuspend(user, tokenProvider, timeoutMilliseconds).also {
                logger.v { "[switchUser] completed('${user.id}')" }
            }
        }
    }

    @CheckResult
    @JvmOverloads
    public fun switchUser(
        user: User,
        token: String,
        timeoutMilliseconds: Long? = null,
        onDisconnectionComplete: () -> Unit = {},
    ): Call<ConnectionData> {
        return switchUser(user, ConstantTokenProvider(token), timeoutMilliseconds, onDisconnectionComplete)
    }

    @CheckResult
    @JvmOverloads
    public fun connectUser(
        user: User,
        token: String,
        timeoutMilliseconds: Long? = null,
    ): Call<ConnectionData> {
        return connectUser(user, ConstantTokenProvider(token), timeoutMilliseconds)
    }

    internal suspend fun setUserWithoutConnectingIfNeeded() {
        if (clientState.initializationState.value == InitializationState.INITIALIZING) {
            delay(INITIALIZATION_DELAY)
            return setUserWithoutConnectingIfNeeded()
        } else if (isUserSet() || clientState.initializationState.value == InitializationState.COMPLETE) {
            logger.d {
                "[setUserWithoutConnectingIfNeeded] User is already set: ${isUserSet()}" +
                    " Initialization state: ${clientState.initializationState.value}"
            }
            return
        }

        userCredentialStorage.get()?.let { config ->
            initializeClientWithUser(
                User(
                    id = config.userId,
                    name = config.userName,
                ),
                tokenProvider = CacheableTokenProvider(ConstantTokenProvider(config.userToken)),
                isAnonymous = config.isAnonymous,
            )
        }
    }

    public fun containsStoredCredentials(): Boolean {
        return userCredentialStorage.get() != null
    }

    private fun storePushNotificationsConfig(userId: String, userName: String, isAnonymous: Boolean) {
        userCredentialStorage.put(
            CredentialConfig(
                userToken = getCurrentToken() ?: "",
                userId = userId,
                userName = userName,
                isAnonymous = isAnonymous,
            ),
        )
    }

    @CheckResult
    @JvmOverloads
    public fun connectAnonymousUser(timeoutMilliseconds: Long? = null): Call<ConnectionData> {
        return CoroutineCall(clientScope) {
            logger.d { "[connectAnonymousUser] no args" }
            userScope.userId.value = ANONYMOUS_USER_ID
            setUser(
                anonUser,
                ConstantTokenProvider(devToken(ANONYMOUS_USER_ID)),
                timeoutMilliseconds,
            ).also { result ->
                logger.v {
                    "[connectAnonymousUser] " +
                        "completed: ${result.stringify { "ConnectionData(connectionId=${it.connectionId})" }}"
                }
            }
        }
    }

    private suspend fun waitFirstConnection(timeoutMilliseconds: Long?): Result<ConnectionData> =
        timeoutMilliseconds?.let {
            withTimeoutOrNull(timeoutMilliseconds) { waitConnection.first() }
                ?: Result.Failure(
                    Error.GenericError("Connection wasn't established in ${timeoutMilliseconds}ms"),
                )
        } ?: waitConnection.first()

    @CheckResult
    @JvmOverloads
    public fun connectGuestUser(
        userId: String,
        username: String,
        timeoutMilliseconds: Long? = null,
    ): Call<ConnectionData> {
        return CoroutineCall(clientScope) {
            logger.d { "[connectGuestUser] userId: '$userId', username: '$username'" }
            userScope.userId.value = userId

            getGuestToken(userId, username).await()
                .flatMapSuspend { setUser(it.user, ConstantTokenProvider(it.token), timeoutMilliseconds) }
                .onSuccess { connectionData ->
                    logger.v {
                        "[connectGuestUser] completed: ConnectionData(connectionId=${connectionData.connectionId})"
                    }
                }
        }
    }

    @CheckResult
    public fun getGuestToken(userId: String, userName: String): Call<GuestUser> {
        return api.getGuestUser(userId, userName)
    }

    @Suppress("LongParameterList")
    @CheckResult
    public fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member> = emptyList(),
    ): Call<List<Member>> {
        logger.d { "[queryMembers] cid: $channelType:$channelId, offset: $offset, limit: $limit" }
        return api.queryMembers(channelType, channelId, offset, limit, filter, sort, members)
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[queryMembers] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onQueryMembersResult(
                        result,
                        channelType,
                        channelId,
                        offset,
                        limit,
                        filter,
                        sort,
                        members,
                    )
                    logger.v { "[queryMembers] result: ${result.stringify { "Members(count=${it.size})" }}" }
                }
            }
            .onQueryMembersError(errorHandlers, channelType, channelId, offset, limit, filter, sort, members)
            .share(userScope) { QueryMembersIdentifier(channelType, channelId, offset, limit, filter, sort, members) }
    }


    @CheckResult
    @JvmOverloads
    public fun sendFile(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback? = null,
    ): Call<UploadedFile> {
        return api.sendFile(channelType, channelId, file, callback)
    }

    @CheckResult
    @JvmOverloads
    public fun sendImage(
        channelType: String,
        channelId: String,
        file: File,
        callback: ProgressCallback? = null,
    ): Call<UploadedFile> {
        return api.sendImage(channelType, channelId, file, callback)
    }

    @CheckResult
    public fun deleteFile(channelType: String, channelId: String, url: String): Call<Unit> {
        return api.deleteFile(channelType, channelId, url)
    }

    @CheckResult
    public fun deleteImage(channelType: String, channelId: String, url: String): Call<Unit> {
        return api.deleteImage(channelType, channelId, url)
    }

    //region Reactions
    @CheckResult
    public fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int,
    ): Call<List<Reaction>> {
        return api.getReactions(messageId, offset, limit)
    }

    @CheckResult
    public fun deleteReaction(messageId: String, reactionType: String, cid: String? = null): Call<Message> {
        val currentUser = getCurrentUser()

        return api.deleteReaction(messageId = messageId, reactionType = reactionType)
            .retry(scope = userScope, retryPolicy = retryPolicy)
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[deleteReaction] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onDeleteReactionRequest(
                        cid = cid,
                        messageId = messageId,
                        reactionType = reactionType,
                        currentUser = currentUser!!,
                    )
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[deleteReaction] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onDeleteReactionResult(
                        cid = cid,
                        messageId = messageId,
                        reactionType = reactionType,
                        currentUser = currentUser!!,
                        result = result,
                    )
                }
            }
            .precondition(plugins) { onDeleteReactionPrecondition(currentUser) }
            .onMessageError(errorHandlers, cid, messageId)
            .share(userScope) { DeleteReactionIdentifier(messageId, reactionType, cid) }
    }


    @CheckResult
    @JvmOverloads
    public fun sendReaction(reaction: Reaction, enforceUnique: Boolean, cid: String? = null): Call<Reaction> {
        val currentUser = getCurrentUser()
        val finalReaction = reaction.copy(createdLocallyAt = Date())
        return api.sendReaction(finalReaction, enforceUnique)
            .retry(scope = userScope, retryPolicy = retryPolicy)
            .doOnStart(userScope) {
                logger.v { "[sendReaction] #doOnStart; reaction: ${reaction.type}, messageId: ${reaction.messageId}" }
                plugins.forEach { plugin ->
                    plugin.onSendReactionRequest(
                        cid = cid,
                        reaction = finalReaction,
                        enforceUnique = enforceUnique,
                        currentUser = currentUser!!,
                    )
                }
            }
            .doOnResult(userScope) { result ->
                logger.v { "[sendReaction] #doOnResult; completed: $result" }
                plugins.forEach { plugin ->
                    plugin.onSendReactionResult(
                        cid = cid,
                        reaction = finalReaction,
                        enforceUnique = enforceUnique,
                        currentUser = currentUser!!,
                        result = result,
                    )
                }
            }
            .onReactionError(errorHandlers, reaction, enforceUnique, currentUser!!)
            .precondition(plugins) { onSendReactionPrecondition(currentUser, reaction) }
            .share(userScope) { SendReactionIdentifier(reaction, enforceUnique, cid) }
    }

    @CheckResult
    public fun disconnectSocket(): Call<Unit> =
        CoroutineCall(userScope) {
            Result.Success(chatSocket.disconnect())
        }

    public fun fetchCurrentUser(): Call<User> {
        return CoroutineCall(userScope) {
            logger.d { "[fetchCurrentUser] isUserSet: ${isUserSet()}, isSocketConnected: ${isSocketConnected()}" }
            when {
                !isUserSet() -> Result.Failure(Error.GenericError("User is not set, can't fetch current user"))
                isSocketConnected() -> Result.Failure(
                    Error.GenericError(
                        "Socket is connected, can't fetch current user",
                    ),
                )

                else -> currentUserFetcher.fetch(getCurrentUser()!!)
            }
        }.doOnResult(userScope) { result ->
            logger.v { "[fetchCurrentUser] completed: $result" }
            result.getOrNull()?.also { currentUser ->
                mutableClientState.setUser(currentUser)
            }
            plugins.forEach { plugin ->
                logger.v { "[fetchCurrentUser] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                plugin.onFetchCurrentUserResult(result)
            }
        }
    }

    @CheckResult
    public fun reconnectSocket(): Call<Unit> =
        CoroutineCall(userScope) {
            when (val userState = userStateService.state) {
                is UserState.UserSet, is UserState.AnonymousUserSet -> Result.Success(
                    chatSocket.reconnectUser(
                        userState.userOrError(),
                        userState is UserState.AnonymousUserSet,
                        true,
                    ),
                )

                else -> Result.Failure(Error.GenericError("Invalid user state $userState without user being set!"))
            }
        }

    public fun addSocketListener(listener: SocketListener) {
        chatSocket.addListener(listener)
    }

    public fun removeSocketListener(listener: SocketListener) {
        chatSocket.removeListener(listener)
    }

    public fun subscribe(
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return eventsObservable.subscribe(listener = listener)
    }

    public fun subscribeFor(
        vararg eventTypes: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        val filter = { event: ChatEvent ->
            event.type in eventTypes
        }
        return eventsObservable.subscribe(filter, listener)
    }


    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        val disposable = subscribeFor(
            *eventTypes,
            listener = { event ->
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    listener.onEvent(event)
                }
            },
        )

        lifecycleOwner.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    disposable.dispose()
                }
            },
        )

        return disposable
    }

    public fun subscribeFor(
        vararg eventTypes: Class<out ChatEvent>,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        val filter = { event: ChatEvent ->
            eventTypes.any { type -> type.isInstance(event) }
        }
        return eventsObservable.subscribe(filter, listener)
    }

    public fun subscribeFor(
        lifecycleOwner: LifecycleOwner,
        vararg eventTypes: Class<out ChatEvent>,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        val disposable = subscribeFor(
            *eventTypes,
            listener = { event ->
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    listener.onEvent(event)
                }
            },
        )

        lifecycleOwner.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    disposable.dispose()
                }
            },
        )

        return disposable
    }

    public fun subscribeForSingle(
        eventType: String,
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        val filter = { event: ChatEvent ->
            event.type == eventType
        }
        return eventsObservable.subscribeSingle(filter, listener)
    }

    public fun <T : ChatEvent> subscribeForSingle(
        eventType: Class<T>,
        listener: ChatEventListener<T>,
    ): Disposable {
        val filter = { event: ChatEvent ->
            eventType.isInstance(event)
        }
        return eventsObservable.subscribeSingle(filter) { event ->
            @Suppress("UNCHECKED_CAST")
            listener.onEvent(event as T)
        }
    }

    @CheckResult
    public fun clearPersistence(): Call<Unit> =
        CoroutineCall(clientScope) {
            disconnectSuspend(true)
            Result.Success(Unit)
        }.doOnStart(clientScope) { setUserWithoutConnectingIfNeeded() }

    @CheckResult
    public fun disconnect(flushPersistence: Boolean): Call<Unit> =
        CoroutineCall(clientScope) {
            logger.d { "[disconnect] flushPersistence: $flushPersistence" }
            when (isUserSet()) {
                true -> {
                    disconnectSuspend(flushPersistence)
                    Result.Success(Unit)
                }

                false -> {
                    logger.i { "[disconnect] cannot disconnect as the user wasn't connected" }
                    Result.Failure(
                        Error.GenericError(
                            message = "ChatClient can't be disconnected because user wasn't connected previously",
                        ),
                    )
                }
            }
        }

    private suspend fun disconnectSuspend(flushPersistence: Boolean) {
        disconnectUserSuspend(flushPersistence)
        userScope.userId.value = null
    }

    private suspend fun disconnectUserSuspend(flushPersistence: Boolean) {
        val userId = getCurrentUser()?.id
        initializedUserId.set(null)
        logger.d { "[disconnectUserSuspend] userId: '$userId', flushPersistence: $flushPersistence" }

        notifications.onLogout(flushPersistence)
        plugins.forEach { it.onUserDisconnected() }
        plugins = emptyList()
        userStateService.onLogout()
        chatSocket.disconnect()
        clientState.awaitConnectionState(ConnectionState.Offline)
        userScope.cancelChildren(userId)

        if (flushPersistence) {
            repositoryFacade.clear()
            userCredentialStorage.clear()
        }

        _repositoryFacade = null
        attachmentsSender.cancelJobs()
        appSettingsManager.clear()
        mutableClientState.clearState()
        audioPlayer.dispose()
        logger.v { "[disconnectUserSuspend] completed('$userId')" }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun ClientState.awaitConnectionState(
        state: ConnectionState,
        timeoutInMillis: Long = DEFAULT_CONNECTION_STATE_TIMEOUT,
    ) = try {
        withTimeout(timeoutInMillis) {
            connectionState.first {
                it == state
            }
        }
    } catch (e: Throwable) {
        logger.e { "[awaitConnectionState] failed: $e" }
    }

    @CheckResult
    public fun getDevices(): Call<List<Device>> {
        return api.getDevices()
            .share(userScope) { GetDevicesIdentifier() }
    }

    @CheckResult
    public fun deleteDevice(device: Device): Call<Unit> {
        return api.deleteDevice(device)
            .share(userScope) { DeleteDeviceIdentifier(device) }
    }

    @CheckResult
    public fun addDevice(device: Device): Call<Unit> {
        return api.addDevice(device)
            .share(userScope) { AddDeviceIdentifier(device) }
    }

    public fun dismissChannelNotifications(channelType: String, channelId: String) {
        notifications.dismissChannelNotifications(channelType, channelId)
    }

    @CheckResult
    public fun searchMessages(
        channelFilter: FilterObject,
        messageFilter: FilterObject,
        offset: Int? = null,
        limit: Int? = null,
        next: String? = null,
        sort: QuerySorter<Message>? = null,
    ): Call<SearchMessagesResult> {
        if (offset != null && (sort != null || next != null)) {
            return ErrorCall(userScope, Error.GenericError("Cannot specify offset with sort or next parameters"))
        }
        return api.searchMessages(
            channelFilter = channelFilter,
            messageFilter = messageFilter,
            offset = offset,
            limit = limit,
            next = next,
            sort = sort,
        )
    }


    @CheckResult
    public fun getPinnedMessages(
        channelType: String,
        channelId: String,
        limit: Int,
        sort: QuerySorter<Message>,
        pagination: PinnedMessagesPagination,
    ): Call<List<Message>> {
        return api.getPinnedMessages(
            channelType = channelType,
            channelId = channelId,
            limit = limit,
            sort = sort,
            pagination = pagination,
        )
    }

    @CheckResult
    public fun getFileAttachments(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
    ): Call<List<Attachment>> =
        getAttachments(channelType, channelId, offset, limit, "file")

    @CheckResult
    public fun getImageAttachments(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
    ): Call<List<Attachment>> =
        getAttachments(channelType, channelId, offset, limit, "image")

    @CheckResult
    private fun getAttachments(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        type: String,
    ): Call<List<Attachment>> =
        getMessagesWithAttachments(channelType, channelId, offset, limit, listOf(type)).map { messages ->
            messages.flatMap { message -> message.attachments.filter { it.type == type } }
        }

    @CheckResult
    public fun getMessagesWithAttachments(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        types: List<String>,
    ): Call<List<Message>> {
        val channelFilter = Filters.`in`("cid", "$channelType:$channelId")
        val messageFilter = Filters.`in`("attachments.type", types)
        return searchMessages(
            channelFilter = channelFilter,
            messageFilter = messageFilter,
            offset = offset,
            limit = limit,
        ).map { it.messages }
    }

    @CheckResult
    public fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        logger.d { "[getReplies] messageId: $messageId, limit: $limit" }

        return api.getReplies(messageId, limit)
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[getReplies] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onGetRepliesRequest(messageId, limit)
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[getReplies] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onGetRepliesResult(result, messageId, limit)
                }
            }
            .precondition(plugins) { onGetRepliesPrecondition(messageId) }
            .share(userScope) { GetRepliesIdentifier(messageId, limit) }
    }

    @CheckResult
    public fun getNewerReplies(
        parentId: String,
        limit: Int,
        lastId: String? = null,
    ): Call<List<Message>> {
        logger.d { "[getNewerReplies] parentId: $parentId, limit: $limit, lastId: $lastId" }

        return api.getNewerReplies(parentId, limit, lastId)
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[getNewerReplies] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onGetNewerRepliesRequest(parentId, limit, lastId)
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[getNewerReplies] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onGetNewerRepliesResult(result, parentId, limit, lastId)
                }
            }
            .precondition(plugins) { onGetRepliesPrecondition(parentId) }
            .share(userScope) { getNewerRepliesIdentifier(parentId, limit, lastId) }
    }

    @CheckResult
    public fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int,
    ): Call<List<Message>> {
        logger.d { "[getRepliesMore] messageId: $messageId, firstId: $firstId, limit: $limit" }

        return api.getRepliesMore(messageId, firstId, limit)
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[getRepliesMore] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onGetRepliesMoreRequest(messageId, firstId, limit)
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[getRepliesMore] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onGetRepliesMoreResult(result, messageId, firstId, limit)
                }
            }
            .precondition(plugins) { onGetRepliesPrecondition(messageId) }
            .share(userScope) { GetRepliesMoreIdentifier(messageId, firstId, limit) }
    }

    @CheckResult
    public fun sendAction(request: SendActionRequest): Call<Message> {
        return api.sendAction(request)
    }

    @CheckResult
    public fun sendGiphy(message: Message): Call<Message> {
        val request = message.run {
            SendActionRequest(cid, id, type, mapOf(KEY_MESSAGE_ACTION to MESSAGE_ACTION_SEND))
        }

        return sendAction(request)
            .retry(scope = userScope, retryPolicy = retryPolicy)
            .doOnResult(userScope) { result ->
                plugins.forEach { listener ->
                    logger.v { "[sendGiphy] #doOnResult; plugin: ${listener::class.qualifiedName}" }
                    listener.onGiphySendResult(cid = message.cid, result = result)
                }
            }
            .share(userScope) { SendGiphyIdentifier(request) }
    }

    @CheckResult
    public fun shuffleGiphy(message: Message): Call<Message> {
        val request = message.run {
            SendActionRequest(cid, id, type, mapOf(KEY_MESSAGE_ACTION to MESSAGE_ACTION_SHUFFLE))
        }

        return sendAction(request)
            .retry(scope = userScope, retryPolicy = retryPolicy)
            .doOnResult(userScope) { result ->
                plugins.forEach { listener ->
                    logger.v { "[shuffleGiphy] #doOnResult; plugin: ${listener::class.qualifiedName}" }
                    listener.onShuffleGiphyResult(cid = message.cid, result = result)
                }
            }
            .share(userScope) { ShuffleGiphyIdentifier(request) }
    }

    @CheckResult
    @JvmOverloads
    public fun deleteMessage(messageId: String, hard: Boolean = false): Call<Message> {
        logger.d { "[deleteMessage] messageId: $messageId, hard: $hard" }

        return api.deleteMessage(messageId, hard)
            .doOnStart(userScope) {
                plugins.forEach { listener ->
                    logger.v { "[deleteMessage] #doOnStart; plugin: ${listener::class.qualifiedName}" }
                    listener.onMessageDeleteRequest(messageId)
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { listener ->
                    logger.v { "[deleteMessage] #doOnResult; plugin: ${listener::class.qualifiedName}" }
                    listener.onMessageDeleteResult(messageId, result)
                }
            }
            .precondition(plugins) {
                onMessageDeletePrecondition(messageId)
            }
            .share(userScope) { DeleteMessageIdentifier(messageId, hard) }
    }

    @CheckResult
    public fun getMessage(messageId: String): Call<Message> {
        logger.d { "[getMessage] messageId: $messageId" }

        return api.getMessage(messageId)
            .doOnResult(
                userScope,
            ) { result ->
                plugins.forEach { listener ->
                    logger.v { "[getMessage] #doOnResult; plugin: ${listener::class.qualifiedName}" }
                    listener.onGetMessageResult(messageId, result)
                }
            }
            .share(userScope) { GetMessageIdentifier(messageId) }
    }

    @CheckResult
    public fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message,
        isRetrying: Boolean = false,
    ): Call<Message> {
        return message.copy(createdLocallyAt = message.createdLocallyAt ?: Date())
            .ensureId(getCurrentUser() ?: getStoredUser())
            .let { processedMessage ->
                CoroutineCall(userScope) {
                    val debugger = clientDebugger.debugSendMessage(channelType, channelId, processedMessage, isRetrying)
                    debugger.onStart(processedMessage)
                    sendAttachments(channelType, channelId, processedMessage, isRetrying, debugger)
                        .flatMapSuspend { newMessage ->
                            debugger.onSendStart(newMessage)
                            doSendMessage(channelType, channelId, newMessage).also { result ->
                                debugger.onSendStop(result, newMessage)
                                debugger.onStop(result, newMessage)
                            }
                        }
                }.share(userScope) {
                    SendMessageIdentifier(channelType, channelId, processedMessage.id)
                }
            }
    }

    private suspend fun doSendMessage(
        channelType: String,
        channelId: String,
        message: Message,
    ): Result<Message> {
        return api.sendMessage(channelType, channelId, message)
            .retry(userScope, retryPolicy)
            .doOnResult(userScope) { result ->
                logger.i { "[sendMessage] result: ${result.stringify { it.toString() }}" }
                plugins.forEach { listener ->
                    logger.v { "[sendMessage] #doOnResult; plugin: ${listener::class.qualifiedName}" }
                    listener.onMessageSendResult(result, channelType, channelId, message)
                }
            }.await()
    }

    private suspend fun sendAttachments(
        channelType: String,
        channelId: String,
        message: Message,
        isRetrying: Boolean = false,
        debugger: SendMessageDebugger,
    ): Result<Message> {
        debugger.onInterceptionStart(message)
        val prepareMessageLogic = PrepareMessageLogicImpl(clientState, logicRegistry)

        val preparedMessage = getCurrentUser()?.let { user ->
            prepareMessageLogic.prepareMessage(message, channelId, channelType, user)
        } ?: message
        debugger.onInterceptionUpdate(preparedMessage)

        plugins.forEach { listener -> listener.onAttachmentSendRequest(channelType, channelId, preparedMessage) }

        return attachmentsSender
            .sendAttachments(preparedMessage, channelType, channelId, isRetrying)
            .also { result ->
                debugger.onInterceptionStop(result, preparedMessage)
            }
    }

    @CheckResult
    public fun updateMessage(message: Message): Call<Message> {
        return api.updateMessage(message)
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[updateMessage] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onMessageEditRequest(message)
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[updateMessage] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onMessageEditResult(message, result)
                }
            }
            .share(userScope) { UpdateMessageIdentifier(message) }
    }

    @CheckResult
    public fun partialUpdateMessage(
        messageId: String,
        set: Map<String, Any> = emptyMap(),
        unset: List<String> = emptyList(),
    ): Call<Message> {
        return api.partialUpdateMessage(
            messageId = messageId,
            set = set,
            unset = unset,
        )
    }

    @CheckResult
    public fun pinMessage(message: Message, expirationDate: Date? = null): Call<Message> {
        val set: MutableMap<String, Any> = LinkedHashMap()
        set["pinned"] = true
        expirationDate?.let { set["pin_expires"] = it }
        return partialUpdateMessage(
            messageId = message.id,
            set = set,
        )
    }

    @CheckResult
    public fun pinMessage(message: Message, timeout: Int): Call<Message> {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.SECOND, timeout)
        }
        return partialUpdateMessage(
            messageId = message.id,
            set = mapOf(
                "pinned" to true,
                "pin_expires" to calendar.time,
            ),
        )
    }

    @CheckResult
    public fun unpinMessage(message: Message): Call<Message> {
        return partialUpdateMessage(
            messageId = message.id,
            set = mapOf("pinned" to false),
        )
    }

    @CheckResult
    public fun queryChannelsInternal(request: QueryChannelsRequest): Call<List<Channel>> {
        return api.queryChannels(request)
    }

    public fun getChannel(
        cid: String,
        messageLimit: Int = 0,
        memberLimit: Int = 0,
        state: Boolean = false,
    ): Call<Channel> {
        return CoroutineCall(userScope) {
            val request = QueryChannelsRequest(
                filter = Filters.eq("cid", cid),
                limit = 1,
                messageLimit = messageLimit,
                memberLimit = memberLimit,
            ).apply {
                this.watch = false
                this.state = state
            }
            when (val result = api.queryChannels(request).await()) {
                is Result.Success -> {
                    val channels = result.value
                    if (channels.isEmpty()) {
                        val cause = StreamChannelNotFoundException(cid)
                        Result.Failure(Error.ThrowableError(cause.message, cause))
                    } else {
                        Result.Success(channels.first())
                    }
                }
                is Result.Failure -> result
            }
        }
    }

    public fun getChannel(
        channelType: String,
        channelId: String,
        messageLimit: Int = 0,
        memberLimit: Int = 0,
        state: Boolean = false,
    ): Call<Channel> {
        return getChannel(cid = "$channelType:$channelId")
    }

    @CheckResult
    private fun queryChannelInternal(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Call<Channel> = api.queryChannel(channelType, channelId, request)

    @CheckResult
    public fun queryChannel(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
        skipOnRequest: Boolean = false,
    ): Call<Channel> {
        return queryChannelInternal(channelType = channelType, channelId = channelId, request = request)
            .doOnStart(userScope) {
                logger.d {
                    "[queryChannel] #doOnStart; skipOnRequest: $skipOnRequest" +
                        ", cid: $channelType:$channelId, request: $request"
                }
                if (!skipOnRequest) {
                    plugins.forEach { plugin ->
                        plugin.onQueryChannelRequest(channelType, channelId, request)
                    }
                }
            }.doOnResult(userScope) { result ->
                logger.v {
                    "[queryChannel] #doOnResult; " +
                        "completed($channelType:$channelId): ${result.errorOrNull() ?: Unit}"
                }
                plugins.forEach { plugin ->
                    plugin.onQueryChannelResult(result, channelType, channelId, request)
                }
            }.precondition(plugins) {
                onQueryChannelPrecondition(channelType, channelId, request)
            }.share(userScope) {
                QueryChannelIdentifier(channelType, channelId, request)
            }
    }


    @CheckResult
    public fun queryChannels(request: QueryChannelsRequest): Call<List<Channel>> {
        logger.d { "[queryChannels] offset: ${request.offset}, limit: ${request.limit}" }
        return queryChannelsInternal(request = request).doOnStart(userScope) {
            plugins.forEach { listener ->
                logger.v { "[queryChannels] #doOnStart; plugin: ${listener::class.qualifiedName}" }
                listener.onQueryChannelsRequest(request)
            }
        }.doOnResult(userScope) { result ->
            plugins.forEach { listener ->
                logger.v { "[queryChannels] #doOnResult; plugin: ${listener::class.qualifiedName}" }
                listener.onQueryChannelsResult(result, request)
            }
        }.precondition(plugins) {
            onQueryChannelsPrecondition(request)
        }.share(userScope) {
            QueryChannelsIdentifier(request)
        }
    }

    @CheckResult
    public fun deleteChannel(channelType: String, channelId: String): Call<Channel> {
        return api.deleteChannel(channelType, channelId)
            .doOnStart(userScope) {
                logger.d { "[deleteChannel] #doOnStart; cid: $channelType:$channelId" }
                plugins.forEach { listener ->
                    listener.onDeleteChannelRequest(getCurrentUser(), channelType, channelId)
                }
            }
            .doOnResult(userScope) { result ->
                logger.v { "[deleteChannel] #doOnResult; completed($channelType:$channelId): $result" }
                plugins.forEach { listener ->
                    listener.onDeleteChannelResult(channelType, channelId, result)
                }
            }
            .precondition(plugins) {
                onDeleteChannelPrecondition(getCurrentUser(), channelType, channelId)
            }
    }

    @CheckResult
    public fun markMessageRead(
        channelType: String,
        channelId: String,
        messageId: String,
    ): Call<Unit> {
        return api.markRead(channelType, channelId, messageId)
            .doOnStart(userScope) {
                logger.d { "[markMessageRead] #doOnStart; cid: $channelType:$channelId, msgId: $messageId" }
            }
            .doOnResult(userScope) {
                logger.v { "[markMessageRead] #doOnResult; completed($channelType:$channelId-$messageId): $it" }
            }
    }

    @CheckResult
    public fun showChannel(channelType: String, channelId: String): Call<Unit> {
        return api.showChannel(channelType, channelId)
    }

    @CheckResult
    public fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean = false,
    ): Call<Unit> {
        logger.d { "[hideChannel] cid: $channelType:$channelId, clearHistory: $clearHistory" }
        return api.hideChannel(channelType, channelId, clearHistory)
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[hideChannel] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onHideChannelRequest(channelType, channelId, clearHistory)
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[hideChannel] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onHideChannelResult(result, channelType, channelId, clearHistory)
                }
            }
            .precondition(plugins) { onHideChannelPrecondition(channelType, channelId, clearHistory) }
            .share(userScope) { HideChannelIdentifier(channelType, channelId, clearHistory) }
    }

    @CheckResult
    public fun truncateChannel(
        channelType: String,
        channelId: String,
        systemMessage: Message? = null,
    ): Call<Channel> {
        return api.truncateChannel(
            channelType = channelType,
            channelId = channelId,
            systemMessage = systemMessage,
        )
    }

    @CheckResult
    public fun stopWatching(channelType: String, channelId: String): Call<Unit> {
        return api.stopWatching(channelType, channelId)
    }

    @CheckResult
    public fun updateChannel(
        channelType: String,
        channelId: String,
        updateMessage: Message?,
        channelExtraData: Map<String, Any> = emptyMap(),
    ): Call<Channel> =
        api.updateChannel(
            channelType,
            channelId,
            channelExtraData,
            updateMessage,
        )

    @CheckResult
    public fun updateChannelPartial(
        channelType: String,
        channelId: String,
        set: Map<String, Any> = emptyMap(),
        unset: List<String> = emptyList(),
    ): Call<Channel> {
        return api.updateChannelPartial(
            channelType = channelType,
            channelId = channelId,
            set = set,
            unset = unset,
        )
    }

    @CheckResult
    public fun enableSlowMode(
        channelType: String,
        channelId: String,
        cooldownTimeInSeconds: Int,
    ): Call<Channel> {
        return if (cooldownTimeInSeconds in 1..MAX_COOLDOWN_TIME_SECONDS) {
            api.enableSlowMode(channelType, channelId, cooldownTimeInSeconds)
        } else {
            ErrorCall(
                userScope,
                Error.GenericError(
                    "You can't specify a value outside the range 1-$MAX_COOLDOWN_TIME_SECONDS for cooldown duration.",
                ),
            )
        }
    }

    @CheckResult
    public fun disableSlowMode(
        channelType: String,
        channelId: String,
    ): Call<Channel> {
        return api.disableSlowMode(channelType, channelId)
    }

    @CheckResult
    public fun rejectInvite(channelType: String, channelId: String): Call<Channel> {
        return api.rejectInvite(channelType, channelId)
    }

    @CheckResult
    public fun sendEvent(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any> = emptyMap(),
    ): Call<ChatEvent> = api.sendEvent(eventType, channelType, channelId, extraData)

    @CheckResult
    public fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String?,
    ): Call<Channel> {
        return api.acceptInvite(channelType, channelId, message)
    }

    @CheckResult
    public fun markAllRead(): Call<Unit> {
        return api.markAllRead()
            .doOnStart(userScope) {
                logger.d { "[markAllRead] #doOnStart; no args" }
                plugins.forEach { it.onMarkAllReadRequest() }
            }
            .doOnResult(userScope) {
                logger.v { "[markAllRead] #doOnResult; completed" }
            }
            .share(userScope) { MarkAllReadIdentifier() }
    }

    @CheckResult
    public fun markRead(channelType: String, channelId: String): Call<Unit> {
        return api.markRead(channelType, channelId)
            .precondition(plugins) { onChannelMarkReadPrecondition(channelType, channelId) }
            .doOnStart(userScope) {
                logger.d { "[markRead] #doOnStart; cid: $channelType:$channelId" }
            }
            .doOnResult(userScope) {
                logger.v { "[markRead] #doOnResult; completed($channelType:$channelId): $it" }
            }
            .share(userScope) { MarkReadIdentifier(channelType, channelId) }
    }

    @CheckResult
    public fun markUnread(
        channelType: String,
        channelId: String,
        messageId: String,
    ): Call<Unit> {
        return api.markUnread(channelType, channelId, messageId)
            .doOnStart(userScope) {
                logger.d { "[markUnread] #doOnStart; cid: $channelType:$channelId, msgId: $messageId" }
            }
            .doOnResult(userScope) {
                logger.v { "[markUnread] #doOnResult; completed($channelType:$channelId, $messageId): $it" }
            }
    }

    @CheckResult
    public fun updateUsers(users: List<User>): Call<List<User>> {
        return api.updateUsers(users)
    }

    @CheckResult
    public fun updateUser(user: User): Call<User> {
        return updateUsers(listOf(user)).map { it.first() }
    }

    @CheckResult
    public fun partialUpdateUser(
        id: String,
        set: Map<String, Any> = emptyMap(),
        unset: List<String> = emptyList(),
    ): Call<User> {
        if (id != getCurrentUser()?.id) {
            val errorMessage = "The client-side partial update allows you to update only the current user. " +
                "Make sure the user is set before updating it."
            logger.e { errorMessage }
            return ErrorCall(userScope, Error.GenericError(errorMessage))
        }

        return api.partialUpdateUser(
            id = id,
            set = set,
            unset = unset,
        )
    }

    @CheckResult
    public fun queryUsers(query: QueryUsersRequest): Call<List<User>> {
        return api.queryUsers(query)
    }

    @CheckResult
    public fun addMembers(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        systemMessage: Message? = null,
        hideHistory: Boolean? = null,
        skipPush: Boolean? = null,
    ): Call<Channel> {
        return api.addMembers(
            channelType,
            channelId,
            memberIds,
            systemMessage,
            hideHistory,
            skipPush,
        )
    }

    @CheckResult
    public fun removeMembers(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        systemMessage: Message? = null,
        skipPush: Boolean? = null,
    ): Call<Channel> = api.removeMembers(
        channelType,
        channelId,
        memberIds,
        systemMessage,
        skipPush,
    )

    @CheckResult
    public fun inviteMembers(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        systemMessage: Message? = null,
        skipPush: Boolean? = null,
    ): Call<Channel> = api.inviteMembers(
        channelType,
        channelId,
        memberIds,
        systemMessage,
        skipPush,
    )

    @JvmOverloads
    @CheckResult
    public fun muteChannel(
        channelType: String,
        channelId: String,
        expiration: Int? = null,
    ): Call<Unit> {
        return api.muteChannel(
            channelType = channelType,
            channelId = channelId,
            expiration = expiration,
        )
    }

    @CheckResult
    public fun unmuteChannel(
        channelType: String,
        channelId: String,
    ): Call<Unit> {
        return api.unmuteChannel(channelType, channelId)
    }

    @JvmOverloads
    @CheckResult
    public fun muteUser(
        userId: String,
        timeout: Int? = null,
    ): Call<Mute> {
        return api.muteUser(userId, timeout)
    }

    @CheckResult
    public fun unmuteUser(userId: String): Call<Unit> {
        return api.unmuteUser(userId)
    }

    @CheckResult
    public fun unmuteCurrentUser(): Call<Unit> = api.unmuteCurrentUser()

    @CheckResult
    public fun muteCurrentUser(): Call<Mute> = api.muteCurrentUser()

    @CheckResult
    public fun flagUser(
        userId: String,
        reason: String?,
        customData: Map<String, String>,
    ): Call<Flag> = api.flagUser(
        userId,
        reason,
        customData,
    )

    @CheckResult
    public fun unflagUser(userId: String): Call<Flag> = api.unflagUser(userId)

    @CheckResult
    public fun flagMessage(
        messageId: String,
        reason: String?,
        customData: Map<String, String>,
    ): Call<Flag> = api.flagMessage(
        messageId,
        reason,
        customData,
    )

    @CheckResult
    public fun unflagMessage(messageId: String): Call<Flag> = api.unflagMessage(messageId)

    @CheckResult
    public fun translate(messageId: String, language: String): Call<Message> =
        api.translate(messageId, language)

    @CheckResult
    public fun enrichUrl(url: String): Call<Attachment> = api.og(url)
        .doOnStart(userScope) {
            logger.d { "[enrichUrl] #doOnStart; url: $url" }
        }
        .doOnResult(userScope) {
            logger.v { "[enrichUrl] #doOnResult; completed($url): $it" }
        }

    @CheckResult
    public fun banUser(
        targetId: String,
        channelType: String,
        channelId: String,
        reason: String?,
        timeout: Int?,
    ): Call<Unit> = api.banUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId,
        reason = reason,
        timeout = timeout,
        shadow = false,
    ).toUnitCall()

    @CheckResult
    public fun unbanUser(
        targetId: String,
        channelType: String,
        channelId: String,
    ): Call<Unit> = api.unbanUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId,
        shadow = false,
    ).toUnitCall()

    @CheckResult
    public fun shadowBanUser(
        targetId: String,
        channelType: String,
        channelId: String,
        reason: String?,
        timeout: Int?,
    ): Call<Unit> = api.banUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId,
        reason = reason,
        timeout = timeout,
        shadow = true,
    ).toUnitCall()

    @CheckResult
    public fun removeShadowBan(
        targetId: String,
        channelType: String,
        channelId: String,
    ): Call<Unit> = api.unbanUser(
        targetId = targetId,
        channelType = channelType,
        channelId = channelId,
        shadow = true,
    ).toUnitCall()

    @CheckResult
    @JvmOverloads
    public fun queryBannedUsers(
        filter: FilterObject,
        sort: QuerySorter<BannedUsersSort> = QuerySortByField.ascByName("created_at"),
        offset: Int? = null,
        limit: Int? = null,
        createdAtAfter: Date? = null,
        createdAtAfterOrEqual: Date? = null,
        createdAtBefore: Date? = null,
        createdAtBeforeOrEqual: Date? = null,
    ): Call<List<BannedUser>> {
        return api.queryBannedUsers(
            filter = filter,
            sort = sort,
            offset = offset,
            limit = limit,
            createdAtAfter = createdAtAfter,
            createdAtAfterOrEqual = createdAtAfterOrEqual,
            createdAtBefore = createdAtBefore,
            createdAtBeforeOrEqual = createdAtBeforeOrEqual,
        )
    }

    internal fun getStoredUser(): User? = userCredentialStorage.get()?.let {
        User(id = it.userId, name = it.userName)
    }

    public fun setPushNotificationReceivedListener(pushNotificationReceivedListener: PushNotificationReceivedListener) {
        this.pushNotificationReceivedListener = pushNotificationReceivedListener
    }

    public fun getConnectionId(): String? {
        return runCatching { chatSocket.connectionIdOrError() }.getOrNull()
    }

    public fun getCurrentUser(): User? {
        return runCatching { userStateService.state.userOrError() }.getOrNull()
    }

    public fun getCurrentToken(): String? {
        return runCatching {
            when (userStateService.state) {
                is UserState.UserSet -> if (tokenManager.hasToken()) tokenManager.getToken() else null
                else -> null
            }
        }.getOrNull()
    }

    public fun getAppSettings(): AppSettings {
        return appSettingsManager.getAppSettings()
    }

    public fun isSocketConnected(): Boolean = chatSocket.isConnected()

    public fun channel(channelType: String, channelId: String): ChannelClient {
        return ChannelClient(channelType, channelId, this)
    }

    public fun channel(cid: String): ChannelClient {
        val (type, id) = cid.cidToTypeAndId()
        return channel(type, id)
    }

    @CheckResult
    public fun createChannel(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        extraData: Map<String, Any>,
    ): Call<Channel> {
        val currentUser = getCurrentUser()

        val request = QueryChannelRequest()
            .withData(extraData + mapOf(QueryChannelRequest.KEY_MEMBERS to memberIds))
        return queryChannelInternal(
            channelType = channelType,
            channelId = channelId,
            request = request,
        )
            .retry(scope = userScope, retryPolicy = retryPolicy)
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[createChannel] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onCreateChannelRequest(
                        channelType = channelType,
                        channelId = channelId,
                        memberIds = memberIds,
                        extraData = extraData,
                        currentUser = currentUser!!,
                    )
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[createChannel] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onCreateChannelResult(
                        channelType = channelType,
                        channelId = channelId,
                        memberIds = memberIds,
                        result = result,
                    )
                }
            }
            .onCreateChannelError(
                errorHandlers = errorHandlers,
                channelType = channelType,
                channelId = channelId,
                memberIds = memberIds,
                extraData = extraData,
            )
            .precondition(plugins) {
                onCreateChannelPrecondition(
                    currentUser = currentUser,
                    channelId = channelId,
                    memberIds = memberIds,
                )
            }
            .share(userScope) { QueryChannelIdentifier(channelType, channelId, request) }
    }

    @CheckResult
    public fun getSyncHistory(
        channelsIds: List<String>,
        lastSyncAt: Date,
    ): Call<List<ChatEvent>> {
        val stringDate = streamDateFormatter.format(lastSyncAt)

        return api.getSyncHistory(channelsIds, stringDate)
            .withPrecondition(userScope) {
                checkSyncHistoryPreconditions(channelsIds, lastSyncAt)
            }
    }

    @CheckResult
    public fun getSyncHistory(
        channelsIds: List<String>,
        lastSyncAt: String,
    ): Call<List<ChatEvent>> {
        val parsedDate = streamDateFormatter.parse(lastSyncAt) ?: return ErrorCall(
            userScope,
            Error.GenericError(
                "The string for data: $lastSyncAt could not be parsed for format: ${streamDateFormatter.datePattern}",
            ),
        )

        return api.getSyncHistory(channelsIds, lastSyncAt)
            .withPrecondition(userScope) {
                checkSyncHistoryPreconditions(channelsIds, parsedDate)
            }
    }

    private fun checkSyncHistoryPreconditions(channelsIds: List<String>, lastSyncAt: Date): Result<Unit> {
        return when {
            channelsIds.isEmpty() -> {
                Result.Failure(Error.GenericError("channelsIds must contain at least 1 id."))
            }

            lastSyncAt.isLaterThanDays(THIRTY_DAYS_IN_MILLISECONDS) -> {
                Result.Failure(Error.GenericError("lastSyncAt cannot by later than 30 days."))
            }

            else -> {
                Result.Success(Unit)
            }
        }
    }

    @CheckResult
    public fun keystroke(channelType: String, channelId: String, parentId: String? = null): Call<ChatEvent> {
        val currentUser = clientState.user.value
        if (currentUser?.privacySettings?.typingIndicators?.enabled == false) {
            logger.v { "[keystroke] rejected (typing indicators are disabled)" }
            return ErrorCall(
                userScope,
                Error.GenericError("Typing indicators are disabled for the current user."),
            )
        }
        val extraData: Map<Any, Any> = parentId?.let {
            mapOf(ARG_TYPING_PARENT_ID to parentId)
        } ?: emptyMap()
        val eventTime = Date()
        val eventType = EventType.TYPING_START
        return api.sendEvent(
            eventType = eventType,
            channelType = channelType,
            channelId = channelId,
            extraData = extraData,
        )
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[keystroke] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onTypingEventRequest(eventType, channelType, channelId, extraData, eventTime)
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[keystroke] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onTypingEventResult(result, eventType, channelType, channelId, extraData, eventTime)
                }
            }
            .precondition(plugins) {
                this.onTypingEventPrecondition(eventType, channelType, channelId, extraData, eventTime)
            }
            .share(userScope) { SendEventIdentifier(eventType, channelType, channelId, parentId) }
    }

    @CheckResult
    public fun stopTyping(channelType: String, channelId: String, parentId: String? = null): Call<ChatEvent> {
        val currentUser = clientState.user.value
        if (currentUser?.privacySettings?.typingIndicators?.enabled == false) {
            logger.v { "[stopTyping] rejected (typing indicators are disabled)" }
            return ErrorCall(
                userScope,
                Error.GenericError("Typing indicators are disabled for the current user."),
            )
        }
        val extraData: Map<Any, Any> = parentId?.let {
            mapOf(ARG_TYPING_PARENT_ID to parentId)
        } ?: emptyMap()
        val eventTime = Date()
        val eventType = EventType.TYPING_STOP
        return api.sendEvent(
            eventType = eventType,
            channelType = channelType,
            channelId = channelId,
            extraData = extraData,
        )
            .doOnStart(userScope) {
                plugins.forEach { plugin ->
                    logger.v { "[stopTyping] #doOnStart; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onTypingEventRequest(eventType, channelType, channelId, extraData, eventTime)
                }
            }
            .doOnResult(userScope) { result ->
                plugins.forEach { plugin ->
                    logger.v { "[stopTyping] #doOnResult; plugin: ${plugin::class.qualifiedName}" }
                    plugin.onTypingEventResult(result, eventType, channelType, channelId, extraData, eventTime)
                }
            }
            .precondition(plugins) {
                this.onTypingEventPrecondition(eventType, channelType, channelId, extraData, eventTime)
            }
            .share(userScope) { SendEventIdentifier(eventType, channelType, channelId, parentId) }
    }

    @CheckResult
    public fun createVideoCall(
        channelType: String,
        channelId: String,
        callType: String,
        callId: String,
    ): Call<VideoCallInfo> {
        return api.createVideoCall(
            channelType = channelType,
            channelId = channelId,
            callType = callType,
            callId = callId,
        )
    }

    @CheckResult
    public fun getVideoCallToken(callId: String): Call<VideoCallToken> {
        return api.getVideoCallToken(callId = callId)
    }

    @CheckResult
    public fun downloadFile(fileUrl: String): Call<ResponseBody> {
        return api.downloadFile(fileUrl)
    }

    private fun warmUp() {
        if (config.warmUp) {
            api.warmUp()
        }
    }

    private fun isUserSet() = userStateService.state !is UserState.NotSet

    public fun devToken(userId: String): String = tokenUtils.devToken(userId)

    @CheckResult
    internal fun <R, T : Any> Call<T>.precondition(
        pluginsList: List<R>,
        preconditionCheck: suspend R.() -> Result<Unit>,
    ): Call<T> = withPrecondition(userScope) {
        pluginsList.map { plugin ->
            plugin.preconditionCheck()
        }.firstOrNull { it is Result.Failure } ?: Result.Success(Unit)
    }

    @Suppress("TooManyFunctions")
    public class Builder(private val apiKey: String, private val appContext: Context) : ChatClientBuilder() {

        private var baseUrl: String = "chat.stream-io-api.com"
        private var cdnUrl: String = baseUrl
        private var logLevel = ChatLogLevel.NOTHING
        private var warmUp: Boolean = true
        private var loggerHandler: ChatLoggerHandler? = null
        private var clientDebugger: ChatClientDebugger? = null
        private var notificationsHandler: NotificationHandler? = null
        private var notificationConfig: NotificationConfig = NotificationConfig(pushNotificationsEnabled = false)
        private var fileUploader: FileUploader? = null
        private val tokenManager: TokenManager = TokenManagerImpl()
        private var customOkHttpClient: OkHttpClient? = null
        private var userCredentialStorage: UserCredentialStorage? = null
        private var retryPolicy: RetryPolicy = NoRetryPolicy()
        private var distinctApiCalls: Boolean = true
        private var debugRequests: Boolean = false
        private var repositoryFactoryProvider: RepositoryFactory.Provider? = null
        private var uploadAttachmentsNetworkType = UploadAttachmentsNetworkType.CONNECTED

        public fun logLevel(level: ChatLogLevel): Builder {
            logLevel = level
            return this
        }

        public fun loggerHandler(loggerHandler: ChatLoggerHandler): Builder {
            this.loggerHandler = loggerHandler
            return this
        }

        public fun clientDebugger(clientDebugger: ChatClientDebugger): Builder {
            this.clientDebugger = clientDebugger
            return this
        }

        @JvmOverloads
        public fun notifications(
            notificationConfig: NotificationConfig,
            notificationsHandler: NotificationHandler =
                NotificationHandlerFactory.createNotificationHandler(
                    context = appContext,
                    notificationConfig = notificationConfig,
                ),
        ): Builder = apply {
            this.notificationConfig = notificationConfig
            this.notificationsHandler = notificationsHandler
        }

        public fun fileUploader(fileUploader: FileUploader): Builder {
            this.fileUploader = fileUploader
            return this
        }

        public fun disableWarmUp(): Builder = apply {
            warmUp = false
        }


        public fun okHttpClient(okHttpClient: OkHttpClient): Builder = apply {
            this.customOkHttpClient = okHttpClient
        }

        public fun baseUrl(value: String): Builder {
            var baseUrl = value
            if (baseUrl.startsWith("https://")) {
                baseUrl = baseUrl.split("https://").toTypedArray()[1]
            }
            if (baseUrl.startsWith("http://")) {
                baseUrl = baseUrl.split("http://").toTypedArray()[1]
            }
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length - 1)
            }
            this.baseUrl = baseUrl
            return this
        }

        public fun withRepositoryFactoryProvider(provider: RepositoryFactory.Provider): Builder = apply {
            repositoryFactoryProvider = provider
        }

        public fun withPlugins(vararg pluginFactories: PluginFactory): Builder = apply {
            this.pluginFactories.addAll(pluginFactories)
        }

        public fun credentialStorage(credentialStorage: UserCredentialStorage): Builder = apply {
            userCredentialStorage = credentialStorage
        }

        public fun debugRequests(shouldDebug: Boolean): Builder = apply {
            this.debugRequests = shouldDebug
        }

        public fun retryPolicy(retryPolicy: RetryPolicy): Builder = apply {
            this.retryPolicy = retryPolicy
        }

        public fun disableDistinctApiCalls(): Builder = apply {
            this.distinctApiCalls = false
        }

        public fun uploadAttachmentsNetworkType(type: UploadAttachmentsNetworkType): Builder = apply {
            this.uploadAttachmentsNetworkType = type
        }

        public override fun build(): ChatClient {
            return super.build()
        }


        @SuppressWarnings("LongMethod")
        override fun internalBuild(): ChatClient {
            if (apiKey.isEmpty()) {
                throw IllegalStateException("apiKey is not defined in " + this::class.java.simpleName)
            }

            instance?.run {
                Log.e(
                    "Chat",
                    "[ERROR] You have just re-initialized ChatClient, old configuration has been overridden [ERROR]",
                )
            }

            val isLocalHost = baseUrl.contains("localhost")
            val httpProtocol = if (isLocalHost) "http" else "https"
            val wsProtocol = if (isLocalHost) "ws" else "wss"
            val lifecycle = ProcessLifecycleOwner.get().lifecycle

            val config = ChatClientConfig(
                apiKey = apiKey,
                httpUrl = "$httpProtocol://$baseUrl/",
                cdnHttpUrl = "$httpProtocol://$cdnUrl/",
                wssUrl = "$wsProtocol://$baseUrl/",
                warmUp = warmUp,
                loggerConfig = ChatLoggerConfigImpl(logLevel, loggerHandler),
                distinctApiCalls = distinctApiCalls,
                debugRequests,
                notificationConfig,
            )
            setupStreamLog()

            if (ToggleService.isInitialized().not()) {
                ToggleService.init(appContext, emptyMap())
            }
            val clientScope = ClientScope()
            val userScope = UserScope(clientScope)

            clientScope.launch {
                warmUpReflection()
            }

            val module =
                ChatModule(
                    appContext = appContext,
                    clientScope = clientScope,
                    userScope = userScope,
                    config = config,
                    notificationsHandler = notificationsHandler ?: NotificationHandlerFactory.createNotificationHandler(
                        context = appContext,
                        notificationConfig = notificationConfig,
                    ),
                    uploader = fileUploader,
                    tokenManager = tokenManager,
                    customOkHttpClient = customOkHttpClient,
                    clientDebugger = clientDebugger,
                    lifecycle = lifecycle,
                )

            val appSettingsManager = AppSettingManager(module.api())

            val audioPlayer: AudioPlayer = StreamMediaPlayer(
                mediaPlayer = NativeMediaPlayerImpl {
                    MediaPlayer().apply {
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                            .let(this::setAudioAttributes)
                    }
                },
                userScope = userScope,
                isMarshmallowOrHigher = { Build.VERSION.SDK_INT >= Build.VERSION_CODES.M },
            )

            return ChatClient(
                config,
                module.api(),
                module.notifications(),
                tokenManager,
                userCredentialStorage = userCredentialStorage ?: SharedPreferencesCredentialStorage(appContext),
                userStateService = module.userStateService,
                clientDebugger = clientDebugger ?: StubChatClientDebugger,
                clientScope = clientScope,
                userScope = userScope,
                retryPolicy = retryPolicy,
                appSettingsManager = appSettingsManager,
                chatSocket = module.chatSocket,
                pluginFactories = pluginFactories,
                repositoryFactoryProvider = repositoryFactoryProvider
                    ?: pluginFactories
                        .filterIsInstance<RepositoryFactory.Provider>()
                        .firstOrNull()
                    ?: NoOpRepositoryFactory.Provider,
                mutableClientState = MutableClientState(module.networkStateProvider),
                currentUserFetcher = module.currentUserFetcher,
                audioPlayer = audioPlayer,
            ).apply {
                attachmentsSender = AttachmentsSender(
                    context = appContext,
                    networkType = uploadAttachmentsNetworkType,
                    clientState = clientState,
                    scope = clientScope,
                )
            }
        }

        private fun setupStreamLog() {
            if (!StreamLog.isInstalled && logLevel != ChatLogLevel.NOTHING) {
                StreamLog.setValidator(StreamLogLevelValidator(logLevel))
                StreamLog.install(
                    CompositeStreamLogger(
                        AndroidStreamLogger(),
                        StreamLoggerHandler(loggerHandler),
                    ),
                )
            }
        }

        private fun warmUpReflection() {
            DownstreamUserDto::class.members
            DownstreamChannelDto::class.members
            DownstreamMessageDto::class.members
            DownstreamReactionDto::class.members
            AttachmentDto::class.members
        }
    }

    abstract class ChatClientBuilder() {

        protected val pluginFactories: MutableList<PluginFactory> = mutableListOf()

        open fun build(): ChatClient = internalBuild()
            .also {
                instance = it
            }

        abstract fun internalBuild(): ChatClient
    }

    public companion object {
        @PublishedApi
        internal const val TAG: String = "Chat:Client"

        @JvmStatic
        public var VERSION_PREFIX_HEADER: VersionPrefixHeader = VersionPrefixHeader.Default

        @JvmStatic
        public var OFFLINE_SUPPORT_ENABLED: Boolean = false

        private const val MAX_COOLDOWN_TIME_SECONDS = 120
        private const val DEFAULT_CONNECTION_STATE_TIMEOUT = 10_000L
        private const val KEY_MESSAGE_ACTION = "image_action"
        private const val MESSAGE_ACTION_SEND = "send"
        private const val MESSAGE_ACTION_SHUFFLE = "shuffle"
        private val THIRTY_DAYS_IN_MILLISECONDS = 30.days.inWholeMilliseconds
        private const val INITIALIZATION_DELAY = 100L

        private const val ARG_TYPING_PARENT_ID = "parent_id"

        private var instance: ChatClient? = null

        @JvmField
        public val DEFAULT_SORT: QuerySorter<Member> = QuerySortByField.descByName("last_updated")

        internal const val ANONYMOUS_USER_ID = "!anon"
        private val anonUser by lazy { User(id = ANONYMOUS_USER_ID) }

        @JvmStatic
        public fun instance(): ChatClient {
            return instance
                ?: throw IllegalStateException(
                    "ChatClient.Builder::build() must be called before obtaining ChatClient instance",
                )
        }

        public val isInitialized: Boolean
            get() = instance != null

        @Throws(IllegalStateException::class)
        @JvmStatic
        public fun handlePushMessage(pushMessage: PushMessage) {
            ensureClientInitialized().run {
                if (!config.notificationConfig.ignorePushMessagesWhenUserOnline || !isSocketConnected()) {
                    clientScope.launch {
                        setUserWithoutConnectingIfNeeded()
                        notifications.onPushMessage(pushMessage, pushNotificationReceivedListener)
                    }
                } else {
                    logger.v { "[handlePushMessage] received push message while WS is connected - ignoring" }
                }
            }
        }

        @Throws(IllegalStateException::class)
        internal fun displayNotification(
            channel: Channel,
            message: Message,
        ) {
            ensureClientInitialized().notifications.displayNotification(
                channel = channel,
                message = message,
            )
        }


        @Throws(IllegalStateException::class)
        internal fun setDevice(device: Device) {
            ensureClientInitialized().notifications.setDevice(device)
        }

        @Throws(IllegalStateException::class)
        private fun ensureClientInitialized(): ChatClient {
            check(isInitialized) { "ChatClient should be initialized first!" }
            return instance()
        }

        internal fun buildSdkTrackingHeaders(): String {
            val clientInformation = VERSION_PREFIX_HEADER.prefix + BuildConfig.STREAM_CHAT_VERSION
            val buildModel = Build.MODEL
            val deviceManufacturer = Build.MANUFACTURER
            val apiLevel = Build.VERSION.SDK_INT
            val osName = "Android ${Build.VERSION.RELEASE}"

            return clientInformation +
                "|os=$osName" +
                "|api_version=$apiLevel" +
                "|device_vendor=$deviceManufacturer" +
                "|device_model=$buildModel" +
                "|offline_enabled=$OFFLINE_SUPPORT_ENABLED"
        }
    }
}
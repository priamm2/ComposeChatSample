package com.example.composechatsample.core.plugin

import android.content.Context
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.repository.DatabaseRepositoryFactory
import com.example.composechatsample.core.repository.DeleteReactionListenerDatabase
import com.example.composechatsample.core.repository.EditMessageListenerDatabase
import com.example.composechatsample.core.repository.HideChannelListenerDatabase
import com.example.composechatsample.core.repository.QueryChannelListenerDatabase
import com.example.composechatsample.core.repository.RepositoryFactory
import com.example.composechatsample.core.repository.SendReactionListenerDatabase
import com.example.composechatsample.core.repository.ThreadQueryListenerDatabase
import com.example.composechatsample.data.ChatDatabase
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.SupervisorJob
import kotlin.reflect.KClass

public class StreamOfflinePluginFactory(private val appContext: Context) : PluginFactory, RepositoryFactory.Provider {

    private val logger by taggedLogger("Chat:OfflinePluginFactory")

    override fun <T : Any> resolveDependency(klass: KClass<T>): T? {
        return when (klass) {
            else -> null
        }
    }

    override fun createRepositoryFactory(user: User): RepositoryFactory {
        logger.d { "[createRepositoryFactory] user.id: '${user.id}'" }
        return DatabaseRepositoryFactory(
            database = createDatabase(appContext, user),
            currentUser = user,
            scope = ChatClient.instance().inheritScope { SupervisorJob(it) },
        )
    }

    override fun get(user: User): Plugin {
        logger.d { "[get] user.id: ${user.id}" }
        return createOfflinePlugin(user)
    }


    @Suppress("LongMethod")
    private fun createOfflinePlugin(user: User): OfflinePlugin {
        logger.v { "[createOfflinePlugin] user.id: ${user.id}" }
        ChatClient.OFFLINE_SUPPORT_ENABLED = true

        val chatClient = ChatClient.instance()
        val clientState = chatClient.clientState
        val repositoryFacade = chatClient.repositoryFacade

        val queryChannelListener = QueryChannelListenerDatabase(repositoryFacade)

        val threadQueryListener = ThreadQueryListenerDatabase(repositoryFacade, repositoryFacade)

        val editMessageListener = EditMessageListenerDatabase(
            userRepository = repositoryFacade,
            messageRepository = repositoryFacade,
            clientState = clientState,
        )

        val hideChannelListener: HideChannelListener = HideChannelListenerDatabase(
            channelRepository = repositoryFacade,
            messageRepository = repositoryFacade,
        )

        val deleteReactionListener: DeleteReactionListener = DeleteReactionListenerDatabase(
            clientState = clientState,
            reactionsRepository = repositoryFacade,
            messageRepository = repositoryFacade,
        )

        val sendReactionListener = SendReactionListenerDatabase(
            clientState = clientState,
            messageRepository = repositoryFacade,
            reactionsRepository = repositoryFacade,
            userRepository = repositoryFacade,
        )

        val deleteMessageListener: DeleteMessageListener = DeleteMessageListenerDatabase(
            clientState = clientState,
            messageRepository = repositoryFacade,
            userRepository = repositoryFacade,
        )

        val sendMessageListener: SendMessageListener = SendMessageListenerDatabase(
            repositoryFacade,
            repositoryFacade,
        )

        val sendAttachmentListener: SendAttachmentListener = SendAttachmentsListenerDatabase(
            repositoryFacade,
            repositoryFacade,
        )

        val shuffleGiphyListener: ShuffleGiphyListener = ShuffleGiphyListenerDatabase(
            userRepository = repositoryFacade,
            messageRepository = repositoryFacade,
        )

        val queryMembersListener: QueryMembersListener = QueryMembersListenerDatabase(
            repositoryFacade,
            repositoryFacade,
        )

        val createChannelListener: CreateChannelListener = CreateChannelListenerDatabase(
            clientState = clientState,
            channelRepository = repositoryFacade,
            userRepository = repositoryFacade,
        )

        val deleteChannelListener: DeleteChannelListener = DeleteChannelListenerDatabase(
            clientState = clientState,
            channelRepository = repositoryFacade,
            userRepository = repositoryFacade,
        )

        val getMessageListener: GetMessageListener = GetMessageListenerDatabase(
            repositoryFacade = repositoryFacade,
        )

        val fetchCurrentUserListener = FetchCurrentUserListenerDatabase(
            userRepository = repositoryFacade,
        )

        return OfflinePlugin(
            activeUser = user,
            queryChannelListener = queryChannelListener,
            threadQueryListener = threadQueryListener,
            editMessageListener = editMessageListener,
            hideChannelListener = hideChannelListener,
            deleteReactionListener = deleteReactionListener,
            sendReactionListener = sendReactionListener,
            deleteMessageListener = deleteMessageListener,
            sendMessageListener = sendMessageListener,
            sendAttachmentListener = sendAttachmentListener,
            shuffleGiphyListener = shuffleGiphyListener,
            queryMembersListener = queryMembersListener,
            createChannelListener = createChannelListener,
            deleteChannelListener = deleteChannelListener,
            getMessageListener = getMessageListener,
            fetchCurrentUserListener = fetchCurrentUserListener,
        )
    }

    private fun createDatabase(
        context: Context,
        user: User,
    ): ChatDatabase {
        return ChatDatabase.getDatabase(context, user.id)
    }
}
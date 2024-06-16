package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.errors.CreateChannelErrorHandlerImpl
import com.example.composechatsample.core.errors.ErrorHandler
import com.example.composechatsample.core.repository.RepositoryFacade
import com.example.composechatsample.core.state.ClientState
import kotlinx.coroutines.CoroutineScope

internal class StateErrorHandlerFactory(
    private val scope: CoroutineScope,
    private val logicRegistry: LogicRegistry,
    private val clientState: ClientState,
    private val repositoryFacade: RepositoryFacade,
) : ErrorHandlerFactory {

    override fun create(): ErrorHandler {
        val deleteReactionErrorHandler = DeleteReactionErrorHandlerImpl(
            scope = scope,
            logic = logicRegistry,
            clientState = clientState,
        )

        val createChannelErrorHandler = CreateChannelErrorHandlerImpl(
            scope = scope,
            clientState = clientState,
            channelRepository = repositoryFacade,
        )

        val queryMembersErrorHandler = QueryMembersErrorHandlerImpl(
            scope = scope,
            clientState = clientState,
            channelRepository = repositoryFacade,
        )

        val sendReactionErrorHandler = SendReactionErrorHandlerImpl(scope = scope, clientState = clientState)

        return StateErrorHandler(
            deleteReactionErrorHandler = deleteReactionErrorHandler,
            createChannelErrorHandler = createChannelErrorHandler,
            queryMembersErrorHandler = queryMembersErrorHandler,
            sendReactionErrorHandler = sendReactionErrorHandler,
        )
    }
}
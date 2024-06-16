package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.errors.CreateChannelErrorHandler
import com.example.composechatsample.core.errors.DeleteReactionErrorHandler
import com.example.composechatsample.core.errors.ErrorHandler
import com.example.composechatsample.core.errors.QueryMembersErrorHandler
import com.example.composechatsample.core.errors.SendReactionErrorHandler

internal class StateErrorHandler(
    private val deleteReactionErrorHandler: DeleteReactionErrorHandler,
    private val createChannelErrorHandler: CreateChannelErrorHandler,
    private val queryMembersErrorHandler: QueryMembersErrorHandler,
    private val sendReactionErrorHandler: SendReactionErrorHandler,
) : ErrorHandler,
    DeleteReactionErrorHandler by deleteReactionErrorHandler,
    CreateChannelErrorHandler by createChannelErrorHandler,
    QueryMembersErrorHandler by queryMembersErrorHandler,
    SendReactionErrorHandler by sendReactionErrorHandler {

    override val priority: Int
        get() = ErrorHandler.DEFAULT_PRIORITY
}
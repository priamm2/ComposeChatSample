package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.Result

public interface DeleteReactionListener {

    public suspend fun onDeleteReactionRequest(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
    )

    public suspend fun onDeleteReactionResult(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
        result: Result<Message>,
    )

    public fun onDeleteReactionPrecondition(currentUser: User?): Result<Unit>
}
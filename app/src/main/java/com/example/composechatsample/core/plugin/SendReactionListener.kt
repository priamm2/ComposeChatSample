package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.Result

public interface SendReactionListener {

    public suspend fun onSendReactionRequest(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    )

    public suspend fun onSendReactionResult(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
        result: Result<Reaction>,
    )

    public suspend fun onSendReactionPrecondition(currentUser: User?, reaction: Reaction): Result<Unit>
}
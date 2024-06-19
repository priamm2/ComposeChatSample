package com.example.composechatsample.core.state

import com.example.composechatsample.core.cidToTypeAndId
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.plugin.DeleteReactionListener
import com.example.composechatsample.core.plugin.LogicRegistry
import com.example.composechatsample.core.removeMyReaction
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.Error
import java.util.Date

internal class DeleteReactionListenerState(
    private val logic: LogicRegistry,
    private val clientState: ClientState,
) : DeleteReactionListener {


    override suspend fun onDeleteReactionRequest(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
    ) {
        val reaction = Reaction(
            messageId = messageId,
            type = reactionType,
            user = currentUser,
            userId = currentUser.id,
            syncStatus = if (clientState.isNetworkAvailable) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED,
            deletedAt = Date(),
        )

        val channelLogic = cid?.cidToTypeAndId()?.let { (type, id) -> logic.channel(type, id) }
            ?: logic.channelFromMessageId(reaction.messageId)
        val cachedChannelMessage = channelLogic?.getMessage(reaction.messageId)
            ?.removeMyReaction(reaction = reaction)
        cachedChannelMessage?.let(channelLogic::upsertMessage)

        val threadLogic = logic.threadFromMessageId(messageId)
        val cachedThreadMessage = threadLogic?.getMessage(reaction.messageId)
            ?.removeMyReaction(reaction = reaction)
        cachedThreadMessage?.let(threadLogic::upsertMessage)
    }


    override suspend fun onDeleteReactionResult(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
        result: Result<Message>,
    ) {

    }

    override fun onDeleteReactionPrecondition(currentUser: User?): Result<Unit> {
        return if (currentUser != null) {
            Result.Success(Unit)
        } else {
            Result.Failure(Error.GenericError(message = "Current user is null!"))
        }
    }
}
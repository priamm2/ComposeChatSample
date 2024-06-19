package com.example.composechatsample.core.state

import com.example.composechatsample.core.addMyReaction
import com.example.composechatsample.core.cidToTypeAndId
import com.example.composechatsample.core.enrichWithDataBeforeSending
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.plugin.LogicRegistry
import com.example.composechatsample.core.plugin.SendReactionListener
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.isPermanent
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.SyncStatus

internal class SendReactionListenerState(
    private val logic: LogicRegistry,
    private val clientState: ClientState,
) : SendReactionListener {

    override suspend fun onSendReactionRequest(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    ) {
        val reactionToSend = reaction.enrichWithDataBeforeSending(
            currentUser = currentUser,
            isOnline = clientState.isNetworkAvailable,
            enforceUnique = enforceUnique,
        )

        val channelLogic = cid?.cidToTypeAndId()?.let { (type, id) -> logic.channel(type, id) }
            ?: logic.channelFromMessageId(reaction.messageId)
        val cachedChannelMessage = channelLogic?.getMessage(reaction.messageId)
            ?.addMyReaction(reaction = reactionToSend, enforceUnique = enforceUnique)
        cachedChannelMessage?.let(channelLogic::upsertMessage)

        val threadLogic = logic.threadFromMessageId(reaction.messageId)
        val cachedThreadMessage = threadLogic?.getMessage(reaction.messageId)
            ?.addMyReaction(reaction = reactionToSend, enforceUnique = enforceUnique)
        cachedThreadMessage?.let(threadLogic::upsertMessage)
    }

    override suspend fun onSendReactionResult(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
        result: Result<Reaction>,
    ) {
        val channelLogic = cid?.cidToTypeAndId()?.let { (type, id) -> logic.channel(type, id) }
            ?: logic.channelFromMessageId(reaction.messageId)
        channelLogic?.getMessage(reaction.messageId)?.let { message ->
            channelLogic.upsertMessage(
                message.updateReactionSyncStatus(
                    originReaction = reaction,
                    result = result,
                ),
            )
        }

        val threadLogic = logic.threadFromMessageId(reaction.messageId)
        threadLogic?.getMessage(reaction.messageId)?.let { message ->
            threadLogic.upsertMessage(
                message.updateReactionSyncStatus(
                    originReaction = reaction,
                    result = result,
                ),
            )
        }
    }

    override suspend fun onSendReactionPrecondition(currentUser: User?, reaction: Reaction): Result<Unit> {
        return when {
            currentUser == null -> {
                Result.Failure(Error.GenericError(message = "Current user is null!"))
            }
            reaction.messageId.isBlank() || reaction.type.isBlank() -> {
                Result.Failure(
                    Error.GenericError(
                        message = "Reaction::messageId and Reaction::type cannot be empty!",
                    ),
                )
            }
            else -> {
                Result.Success(Unit)
            }
        }
    }

    private fun Message.updateReactionSyncStatus(originReaction: Reaction, result: Result<*>): Message = this.copy(
        ownReactions = ownReactions
            .map { ownReaction ->
                when (ownReaction.id) {
                    originReaction.id -> ownReaction.updateSyncStatus(result)
                    else -> ownReaction
                }
            },
        latestReactions = latestReactions
            .map { latestReaction ->
                when (latestReaction.id) {
                    originReaction.id -> latestReaction.updateSyncStatus(result)
                    else -> latestReaction
                }
            },
    )

    private fun Reaction.updateSyncStatus(result: Result<*>): Reaction = this.copy(
        syncStatus = when (result) {
            is Result.Success -> SyncStatus.COMPLETED
            is Result.Failure -> when {
                result.value.isPermanent() -> SyncStatus.FAILED_PERMANENTLY
                else -> SyncStatus.SYNC_NEEDED
            }
        },
    )
}
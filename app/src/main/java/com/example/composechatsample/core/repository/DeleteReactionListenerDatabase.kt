package com.example.composechatsample.core.repository

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.plugin.DeleteReactionListener
import com.example.composechatsample.core.state.ClientState
import java.util.Date
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.removeMyReaction
import com.example.composechatsample.core.updateSyncStatus

internal class DeleteReactionListenerDatabase(
    private val clientState: ClientState,
    private val reactionsRepository: ReactionRepository,
    private val messageRepository: MessageRepository,
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

        reactionsRepository.insertReaction(reaction)

        messageRepository.selectMessage(messageId = messageId)?.copy()?.let { cachedMessage ->
            messageRepository.insertMessage(cachedMessage.removeMyReaction(reaction))
        }
    }

    override suspend fun onDeleteReactionResult(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
        result: Result<Message>,
    ) {
        reactionsRepository.selectUserReactionToMessage(
            reactionType = reactionType,
            messageId = messageId,
            userId = currentUser.id,
        )?.let { cachedReaction ->
            reactionsRepository.insertReaction(cachedReaction.updateSyncStatus(result))
        }
    }

    override fun onDeleteReactionPrecondition(currentUser: User?): Result<Unit> {
        return if (currentUser != null) {
            Result.Success(Unit)
        } else {
            Result.Failure(Error.GenericError(message = "Current user is null!"))
        }
    }
}
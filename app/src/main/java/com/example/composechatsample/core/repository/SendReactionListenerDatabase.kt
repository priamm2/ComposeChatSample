package com.example.composechatsample.core.repository

import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.plugin.SendReactionListener
import com.example.composechatsample.core.state.ClientState
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.enrichWithDataBeforeSending
import com.example.composechatsample.core.updateSyncStatus
import java.util.Date

internal class SendReactionListenerDatabase(
    private val clientState: ClientState,
    private val reactionsRepository: ReactionRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
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

        if (enforceUnique) {
            reactionsRepository.updateReactionsForMessageByDeletedDate(
                userId = currentUser.id,
                messageId = reactionToSend.messageId,
                deletedAt = Date(),
            )
        }

        reaction.user?.let { user -> userRepository.insertUser(user) }
        reactionsRepository.insertReaction(reaction = reactionToSend)

        messageRepository.selectMessage(messageId = reactionToSend.messageId)?.copy()?.let { cachedMessage ->
            messageRepository.insertMessage(
                cachedMessage.addMyReaction(reaction = reactionToSend, enforceUnique = enforceUnique),
            )
        }
    }

    override suspend fun onSendReactionResult(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
        result: Result<Reaction>,
    ) {
        reactionsRepository.selectUserReactionToMessage(
            reactionType = reaction.type,
            messageId = reaction.messageId,
            userId = currentUser.id,
        )?.let { cachedReaction ->
            reactionsRepository.insertReaction(cachedReaction.updateSyncStatus(result))
        }
    }

    override suspend fun onSendReactionPrecondition(currentUser: User?, reaction: Reaction): Result<Unit> {
        return when {
            currentUser == null -> {
                Result.Failure(Error.GenericError(message = "Current user is null!"))
            }
            reaction.messageId.isBlank() || reaction.type.isBlank() -> {
                Result.Failure(Error.GenericError("Reaction::messageId and Reaction::type cannot be empty!"))
            }
            messageRepository.selectMessage(reaction.messageId) == null -> {
                Result.Failure(Error.GenericError("Reaction::messageId cannot be found in DB!"))
            }
            else -> {
                Result.Success(Unit)
            }
        }
    }
}
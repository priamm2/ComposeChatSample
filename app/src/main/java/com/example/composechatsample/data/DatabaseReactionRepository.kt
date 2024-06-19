package com.example.composechatsample.data

import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.SyncStatus
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.repository.ReactionRepository
import com.example.composechatsample.core.toEntity
import com.example.composechatsample.core.toModel
import java.util.Date

internal class DatabaseReactionRepository(
    private val reactionDao: ReactionDao,
    private val getUser: suspend (userId: String) -> User,
) : ReactionRepository {

    override suspend fun insertReaction(reaction: Reaction) {
        require(reaction.messageId.isNotEmpty()) { "message id can't be empty when creating a reaction" }
        require(reaction.type.isNotEmpty()) { "type can't be empty when creating a reaction" }
        require(reaction.userId.isNotEmpty()) { "user id can't be empty when creating a reaction" }

        reactionDao.insert(reaction.toEntity())
    }

    override suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date) {
        reactionDao.setDeleteAt(userId, messageId, deletedAt)
    }

    override suspend fun selectReactionById(id: Int): Reaction? {
        return reactionDao.selectReactionById(id)?.toModel(getUser)
    }

    override suspend fun selectReactionsByIds(ids: List<Int>): List<Reaction> {
        return reactionDao.selectReactionsByIds(ids).map { it.toModel(getUser) }
    }

    override suspend fun selectReactionIdsBySyncStatus(syncStatus: SyncStatus): List<Int> {
        return reactionDao.selectIdsSyncStatus(syncStatus)
    }

    override suspend fun selectReactionsBySyncStatus(syncStatus: SyncStatus): List<Reaction> {
        return reactionDao.selectSyncStatus(syncStatus).map { it.toModel(getUser) }
    }

    override suspend fun selectUserReactionToMessage(
        reactionType: String,
        messageId: String,
        userId: String,
    ): Reaction? {
        return reactionDao.selectUserReactionToMessage(
            reactionType = reactionType,
            messageId = messageId,
            userId = userId,
        )?.toModel(getUser)
    }

    override suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
    ): List<Reaction> {
        return reactionDao.selectUserReactionsToMessage(messageId = messageId, userId = userId)
            .map { it.toModel(getUser) }
    }

    override suspend fun deleteReaction(reaction: Reaction) {
        reactionDao.delete(reaction.toEntity())
    }

    override suspend fun clear() {
        reactionDao.deleteAll()
    }
}
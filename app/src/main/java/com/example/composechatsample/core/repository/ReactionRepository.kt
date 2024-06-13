package com.example.composechatsample.core.repository

import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.SyncStatus
import java.util.Date

public interface ReactionRepository {

    public suspend fun insertReaction(reaction: Reaction)

    public suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date)

    public suspend fun selectReactionById(id: Int): Reaction?

    public suspend fun selectReactionsByIds(ids: List<Int>): List<Reaction>

    public suspend fun selectReactionIdsBySyncStatus(syncStatus: SyncStatus): List<Int>

    public suspend fun selectReactionsBySyncStatus(syncStatus: SyncStatus): List<Reaction>

    public suspend fun selectUserReactionToMessage(reactionType: String, messageId: String, userId: String): Reaction?

    public suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
    ): List<Reaction>

    public suspend fun deleteReaction(reaction: Reaction)

    public suspend fun clear()
}
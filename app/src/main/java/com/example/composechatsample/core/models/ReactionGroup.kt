package com.example.composechatsample.core.models

import java.util.Date

public data class ReactionGroup(
    val type: String,
    val count: Int,
    val sumScore: Int,
    val firstReactionAt: Date,
    val lastReactionAt: Date,
) {

    @SinceKotlin("99999.9")
    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public fun newBuilder(): Builder = Builder(this)

    public class Builder() {
        private var type: String = ""
        private var count: Int = 0
        private var sumScore: Int = 0
        private var firstReactionAt: Date = Date()
        private var lastReactionAt: Date = Date()

        public constructor(reactionGroup: ReactionGroup) : this() {
            type = reactionGroup.type
            count = reactionGroup.count
            sumScore = reactionGroup.sumScore
            firstReactionAt = reactionGroup.firstReactionAt
            lastReactionAt = reactionGroup.lastReactionAt
        }

        public fun type(type: String): Builder = apply { this.type = type }
        public fun count(count: Int): Builder = apply { this.count = count }
        public fun sumScore(sumScore: Int): Builder = apply { this.sumScore = sumScore }
        public fun firstReactionAt(firstReactionAt: Date): Builder = apply { this.firstReactionAt = firstReactionAt }
        public fun lastReactionAt(lastReactionAt: Date): Builder = apply { this.lastReactionAt = lastReactionAt }

        public fun build(): ReactionGroup = ReactionGroup(
            type = type,
            count = count,
            sumScore = sumScore,
            firstReactionAt = firstReactionAt,
            lastReactionAt = lastReactionAt,
        )
    }
}

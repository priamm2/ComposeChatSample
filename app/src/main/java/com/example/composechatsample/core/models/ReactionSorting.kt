package com.example.composechatsample.core.models

import com.example.composechatsample.log.StreamLog
private const val TAG = "Chat:ReactionSorting"
private const val DEBUG = false

public fun interface ReactionSorting : Comparator<ReactionGroup>

public data object ReactionSortingBySumScore : ReactionSorting {
    override fun compare(o1: ReactionGroup, o2: ReactionGroup): Int {
        if (DEBUG) {
            StreamLog.v(TAG) { "[compareBySumScore] o1.type: ${o1.type}, o2.type: ${o2.type}" }
        }
        return o1.sumScore.compareTo(o2.sumScore)
    }
}

public data object ReactionSortingByCount : ReactionSorting {
    override fun compare(o1: ReactionGroup, o2: ReactionGroup): Int {
        if (DEBUG) {
            StreamLog.v(TAG) { "[compareByCount] o1.type: ${o1.type}, o2.type: ${o2.type}" }
        }
        return o1.count.compareTo(o2.count)
    }
}

public data object ReactionSortingByLastReactionAt : ReactionSorting {
    override fun compare(o1: ReactionGroup, o2: ReactionGroup): Int {
        if (DEBUG) {
            StreamLog.v(TAG) { "[compareByLastReactionAt] o1.type: ${o1.type}, o2.type: ${o2.type}" }
        }
        return o1.lastReactionAt.compareTo(o2.lastReactionAt)
    }
}

public data object ReactionSortingByFirstReactionAt : ReactionSorting {
    override fun compare(o1: ReactionGroup, o2: ReactionGroup): Int {
        if (DEBUG) {
            StreamLog.v(TAG) { "[compareByFirstReactionAt] o1.type: ${o1.type}, o2.type: ${o2.type}" }
        }
        return o1.firstReactionAt.compareTo(o2.firstReactionAt)
    }
}

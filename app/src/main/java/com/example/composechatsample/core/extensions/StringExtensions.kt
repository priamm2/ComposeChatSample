package com.example.composechatsample.core.extensions

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.Reaction
import com.example.composechatsample.core.models.User

private val snakeRegex = "_[a-zA-Z]".toRegex()
private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

internal fun String.snakeToLowerCamelCase(): String {
    return snakeRegex.replace(this) { matchResult ->
        matchResult.value.replace("_", "").uppercase()
    }
}

internal fun String.lowerCamelCaseToGetter(): String = "get${this[0].uppercase()}${this.substring(1)}"

internal fun String.camelCaseToSnakeCase(): String {
    return camelRegex.replace(this) { "_${it.value}" }.lowercase()
}

fun Message.users(): List<User> {
    return latestReactions.mapNotNull(Reaction::user) +
            user +
            (replyTo?.users().orEmpty()) +
            mentionedUsers +
            ownReactions.mapNotNull(Reaction::user) +
            threadParticipants +
            (pinnedBy?.let { listOf(it) } ?: emptyList())
}

package com.example.composechatsample.core

import com.example.composechatsample.core.models.User as UserModel

internal sealed class UserState {
    object NotSet : UserState() { override fun toString(): String = "NotSet" }
    data class UserSet(val user: UserModel) : UserState()

    data class AnonymousUserSet(val anonymousUser: UserModel) : UserState()

    internal fun userOrError(): UserModel = when (this) {
        is UserSet -> user
        is AnonymousUserSet -> anonymousUser
        else -> error("This state doesn't contain user!")
    }
}
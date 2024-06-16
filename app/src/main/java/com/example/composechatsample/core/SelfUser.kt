package com.example.composechatsample.core

import com.example.composechatsample.core.models.User

sealed class SelfUser {
    abstract val me: User
}

internal data class SelfUserFull(override val me: User) : SelfUser()

internal data class SelfUserPart(override val me: User) : SelfUser()
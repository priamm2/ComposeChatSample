package com.example.composechatsample.data

import com.example.composechatsample.core.models.User

data class UserCredentials(
    val apiKey: String,
    val user: User,
    val token: String,
)
package com.example.composechatsample.core.repository

import com.example.composechatsample.core.models.User
import kotlinx.coroutines.flow.StateFlow

public interface UserRepository {
    public suspend fun insertUsers(users: Collection<User>)
    public suspend fun insertUser(user: User)
    public suspend fun insertCurrentUser(user: User)
    public suspend fun selectUser(userId: String): User?
    public suspend fun selectUsers(ids: List<String>): List<User>
    public fun observeLatestUsers(): StateFlow<Map<String, User>>
    public suspend fun clear()
}
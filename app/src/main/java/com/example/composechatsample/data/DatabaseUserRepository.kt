package com.example.composechatsample.data

import androidx.collection.LruCache
import com.example.composechatsample.core.launchWithMutex
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.repository.UserRepository
import com.example.composechatsample.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class DatabaseUserRepository(
    private val scope: CoroutineScope,
    private val userDao: UserDao,
    cacheSize: Int = 1000,
) : UserRepository {
    private val logger by taggedLogger("Chat:UserRepository")

    private val userCache = LruCache<String, User>(cacheSize)
    private val latestUsersFlow: MutableStateFlow<Map<String, User>> = MutableStateFlow(emptyMap())
    private val dbMutex = Mutex()

    override fun observeLatestUsers(): StateFlow<Map<String, User>> = latestUsersFlow

    override suspend fun clear() {
        dbMutex.withLock {
            userDao.deleteAll()
        }
    }

    override suspend fun insertUsers(users: Collection<User>) {
        if (users.isEmpty()) return
        val usersToInsert = users
            .filter { it != userCache[it.id] }
            .map { it.toEntity() }
        cacheUsers(users)
        scope.launchWithMutex(dbMutex) {
            logger.v { "[insertUsers] inserting ${usersToInsert.size} entities on DB, updated ${users.size} on cache" }
            usersToInsert
                .takeUnless { it.isEmpty() }
                ?.let { userDao.insertMany(it) }
        }
    }

    override suspend fun insertUser(user: User) {
        insertUsers(listOf(user))
    }

    override suspend fun insertCurrentUser(user: User) {
        insertUser(user)
        scope.launchWithMutex(dbMutex) {
            val userEntity = user.toEntity().copy(id = ME_ID)
            userDao.insert(userEntity)
        }
    }

    override suspend fun selectUser(userId: String): User? {
        return userCache[userId] ?: userDao.select(userId)?.let(::toModel)?.also { cacheUsers(listOf(it)) }
    }

    override suspend fun selectUsers(ids: List<String>): List<User> {
        val cachedUsers = ids.mapNotNullTo(mutableListOf(), userCache::get)
        val missingUserIds = ids.minus(cachedUsers.map(User::id).toSet())

        return cachedUsers + userDao.select(missingUserIds).map(::toModel).also { cacheUsers(it) }
    }

    private fun cacheUsers(users: Collection<User>) {
        for (userEntity in users) {
            userCache.put(userEntity.id, userEntity)
        }
        scope.launch { latestUsersFlow.value = userCache.snapshot() }
    }

    private fun User.toEntity(): UserEntity =
        UserEntity(
            id = id,
            name = name,
            image = image,
            originalId = id,
            role = role,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastActive = lastActive,
            invisible = isInvisible,
            privacySettings = privacySettings?.toEntity(),
            banned = isBanned,
            extraData = extraData,
            mutes = mutes.map { mute -> mute.target.id },
        )

    private fun toModel(userEntity: UserEntity): User = with(userEntity) {
        User(
            id = this.originalId,
            name = name,
            image = image,
            role = role,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastActive = lastActive,
            invisible = invisible,
            privacySettings = privacySettings?.toModel(),
            extraData = extraData.toMutableMap(),
            banned = banned,
        )
    }

    companion object {
        private const val ME_ID = "me"
    }
}
package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.generateChannelIdIfNeeded
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.repository.ChannelRepository
import com.example.composechatsample.core.repository.UserRepository
import com.example.composechatsample.core.state.ClientState
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.isPermanent
import com.example.composechatsample.core.models.SyncStatus
import java.util.Date

internal class CreateChannelListenerDatabase(
    private val clientState: ClientState,
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository,
) : CreateChannelListener {


    override suspend fun onCreateChannelRequest(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        extraData: Map<String, Any>,
        currentUser: User,
    ) {
        val generatedChannelId = generateChannelIdIfNeeded(channelId, memberIds)
        val channel = Channel(
            id = generatedChannelId,
            type = channelType,
            members = getMembers(memberIds),
            extraData = extraData.toMutableMap(),
            createdAt = Date(),
            createdBy = currentUser,
            syncStatus = if (clientState.isOnline) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED,
            name = extraData["name"] as? String ?: "",
            image = extraData["image"] as? String ?: "",
        )

        channelRepository.insertChannel(channel)
    }

    private suspend fun getMembers(memberIds: List<String>): List<Member> {
        val cachedUsers = userRepository.selectUsers(memberIds)
        val missingUserIds = memberIds.minus(cachedUsers.map(User::id).toSet())

        return (cachedUsers + missingUserIds.map(::User)).map(::Member)
    }


    override suspend fun onCreateChannelResult(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        result: Result<Channel>,
    ) {
        val generatedCid = "$channelType:${generateChannelIdIfNeeded(channelId, memberIds)}"
        when (result) {
            is Result.Success -> {
                val channel = result.value.copy(syncStatus = SyncStatus.COMPLETED)
                if (channel.cid != generatedCid) {
                    channelRepository.deleteChannel(generatedCid)
                }
                channelRepository.insertChannel(channel)
            }
            is Result.Failure -> {
                channelRepository.selectChannels(listOf(generatedCid)).firstOrNull()?.let { cachedChannel ->
                    channelRepository.insertChannel(
                        cachedChannel.copy(
                            syncStatus = if (result.value.isPermanent()) {
                                SyncStatus.FAILED_PERMANENTLY
                            } else {
                                SyncStatus.SYNC_NEEDED
                            },
                        ),
                    )
                }
            }
        }
    }

    override fun onCreateChannelPrecondition(
        currentUser: User?,
        channelId: String,
        memberIds: List<String>,
    ): Result<Unit> {
        return when {
            channelId.isBlank() && memberIds.isEmpty() -> {
                Result.Failure(Error.GenericError(message = "Either channelId or memberIds cannot be empty!"))
            }
            currentUser == null -> {
                Result.Failure(Error.GenericError(message = "Current user is null!"))
            }
            else -> {
                Result.Success(Unit)
            }
        }
    }
}
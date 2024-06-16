package com.example.composechatsample.core.errors

import com.example.composechatsample.core.Call
import com.example.composechatsample.core.ReturnOnErrorCall
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.onErrorReturn
import com.example.composechatsample.core.repository.ChannelRepository
import com.example.composechatsample.core.state.ClientState
import kotlinx.coroutines.CoroutineScope
import com.example.composechatsample.core.Error
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.generateChannelIdIfNeeded

internal class CreateChannelErrorHandlerImpl(
    private val scope: CoroutineScope,
    private val channelRepository: ChannelRepository,
    private val clientState: ClientState,
) : CreateChannelErrorHandler {


    override fun onCreateChannelError(
        originalCall: Call<Channel>,
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        extraData: Map<String, Any>,
    ): ReturnOnErrorCall<Channel> {
        return originalCall.onErrorReturn(scope) { originalError ->
            if (clientState.isOnline) {
                Result.Failure(originalError)
            } else {
                val generatedCid =
                    "$channelType:${generateChannelIdIfNeeded(channelId = channelId, memberIds = memberIds)}"
                val cachedChannel = channelRepository.selectChannels(listOf(generatedCid)).firstOrNull()
                if (cachedChannel == null) {
                    Result.Failure(Error.GenericError(message = "Channel wasn't cached properly."))
                } else {
                    Result.Success(cachedChannel)
                }
            }
        }
    }
}
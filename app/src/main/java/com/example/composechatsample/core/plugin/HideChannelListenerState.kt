package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.toCid
import com.example.composechatsample.core.Result
import com.example.composechatsample.core.validateCidWithResult
import java.util.Date

internal class HideChannelListenerState(private val logic: LogicRegistry) : HideChannelListener {

    override suspend fun onHideChannelPrecondition(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Result<Unit> = validateCidWithResult(Pair(channelType, channelId).toCid()).toUnitResult()

    override suspend fun onHideChannelRequest(channelType: String, channelId: String, clearHistory: Boolean) {
        logic.channel(channelType, channelId).stateLogic().toggleHidden(true)
    }

    override suspend fun onHideChannelResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ) {
        val channelStateLogic = logic.channel(channelType, channelId).stateLogic()
        when (result) {
            is Result.Success -> {
                if (clearHistory) {
                    val now = Date()
                    channelStateLogic.run {
                        hideMessagesBefore(now)
                        removeMessagesBefore(now)
                    }
                }
            }
            is Result.Failure -> channelStateLogic.toggleHidden(false)
        }
    }
}
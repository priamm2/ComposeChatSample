package com.example.composechatsample.core.state

import com.example.composechatsample.core.Result
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.plugin.LogicRegistry
import com.example.composechatsample.core.plugin.SendGiphyListener

internal class SendGiphyListenerState(private val logic: LogicRegistry) : SendGiphyListener {


    override fun onGiphySendResult(cid: String, result: Result<Message>) {
        if (result is Result.Success) {
            val message = result.value
            logic.channelFromMessage(message)?.stateLogic()?.deleteMessage(message)
            logic.threadFromMessage(message)?.stateLogic()?.deleteMessage(message)
        }
    }
}
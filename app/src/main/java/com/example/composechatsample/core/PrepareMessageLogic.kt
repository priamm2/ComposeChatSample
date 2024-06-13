package com.example.composechatsample.core

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User

public interface PrepareMessageLogic {

    public fun prepareMessage(message: Message, channelId: String, channelType: String, user: User): Message
}
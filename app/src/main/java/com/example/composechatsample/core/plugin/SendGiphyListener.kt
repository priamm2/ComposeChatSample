package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.Result

public interface SendGiphyListener {
    public fun onGiphySendResult(
        cid: String,
        result: Result<Message>,
    )
}
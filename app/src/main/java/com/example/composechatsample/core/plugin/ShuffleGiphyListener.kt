package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.Result

public interface ShuffleGiphyListener {


    public suspend fun onShuffleGiphyResult(
        cid: String,
        result: Result<Message>,
    )
}
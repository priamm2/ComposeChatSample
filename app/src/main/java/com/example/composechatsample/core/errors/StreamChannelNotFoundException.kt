package com.example.composechatsample.core.errors

public class StreamChannelNotFoundException(
    public val cid: String,
    public override val message: String = "Channel with cid \"$cid\" not found",
) : StreamException(message)
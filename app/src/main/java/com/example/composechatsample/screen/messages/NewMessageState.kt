package com.example.composechatsample.screen.messages

public sealed class NewMessageState

public data class MyOwn(val ts: Long?) : NewMessageState()

public data class Other(val ts: Long?) : NewMessageState()
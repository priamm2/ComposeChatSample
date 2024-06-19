package com.example.composechatsample.screen.messages

public sealed class MessageFocusState

public object MessageFocused : MessageFocusState() { override fun toString(): String = "MessageFocused" }

public object MessageFocusRemoved : MessageFocusState() { override fun toString(): String = "MessageFocusRemoved" }
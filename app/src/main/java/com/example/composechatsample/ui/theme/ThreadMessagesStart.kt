package com.example.composechatsample.ui.theme

import androidx.compose.runtime.Stable

@Stable
public sealed class ThreadMessagesStart {

    @Stable public object TOP : ThreadMessagesStart() {
        override fun toString(): String = "TOP"
    }

    @Stable public object BOTTOM : ThreadMessagesStart() {
        override fun toString(): String = "BOTTOM"
    }
}
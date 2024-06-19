package com.example.composechatsample.ui.theme

import com.example.composechatsample.screen.MessageItemState

public fun interface MessageAlignmentProvider {

    public fun provideMessageAlignment(messageItem: MessageItemState): MessageAlignment

    public companion object {
        public fun defaultMessageAlignmentProvider(): MessageAlignmentProvider {
            return DefaultMessageAlignmentProvider()
        }
    }
}

private class DefaultMessageAlignmentProvider : MessageAlignmentProvider {
    override fun provideMessageAlignment(messageItem: MessageItemState): MessageAlignment {
        return if (messageItem.isMine) MessageAlignment.End else MessageAlignment.Start
    }
}
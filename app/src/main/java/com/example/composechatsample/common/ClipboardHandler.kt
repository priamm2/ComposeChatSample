package com.example.composechatsample.common

import android.content.ClipData
import android.content.ClipboardManager
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User

public fun interface ClipboardHandler {

    public fun copyMessage(message: Message)
}


public class ClipboardHandlerImpl(
    private val clipboardManager: ClipboardManager,
    private val autoTranslationEnabled: Boolean = false,
    private val getCurrentUser: () -> User? = { null },
) : ClipboardHandler {

    override fun copyMessage(message: Message) {
        val displayedText = when (autoTranslationEnabled) {
            true -> getCurrentUser()?.language?.let { userLanguage ->
                message.getTranslation(userLanguage).ifEmpty { message.text }
            } ?: message.text
            else -> message.text
        }
        clipboardManager.setPrimaryClip(ClipData.newPlainText("message", displayedText))
    }
}
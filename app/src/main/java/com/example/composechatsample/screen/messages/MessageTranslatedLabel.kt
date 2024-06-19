package com.example.composechatsample.screen.messages

import androidx.compose.runtime.Composable
import com.example.composechatsample.core.isDeleted
import com.example.composechatsample.core.isGiphy
import com.example.composechatsample.screen.MessageItemState
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun MessageTranslatedLabel(
    messageItem: MessageItemState,
) {
    if (!ChatTheme.autoTranslationEnabled) {
        return
    }
    val userLanguage = messageItem.currentUser?.language.orEmpty()
    val i18nLanguage = messageItem.message.originalLanguage
    val isGiphy = messageItem.message.isGiphy()
    val isDeleted = messageItem.message.isDeleted()
    val translatedText = messageItem.message.getTranslation(userLanguage).ifEmpty { messageItem.message.text }
    if (!isGiphy && !isDeleted && userLanguage != i18nLanguage && translatedText != messageItem.message.text) {
        TranslatedLabel(translatedTo = userLanguage)
    }
}
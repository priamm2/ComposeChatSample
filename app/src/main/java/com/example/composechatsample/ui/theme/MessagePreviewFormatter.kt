package com.example.composechatsample.ui.theme

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.example.composechatsample.core.isSystem
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User

public fun interface MessagePreviewFormatter {

    public fun formatMessagePreview(message: Message, currentUser: User?): AnnotatedString

    public companion object {
        public fun defaultFormatter(
            context: Context,
            autoTranslationEnabled: Boolean,
            typography: StreamTypography,
            attachmentFactories: List<AttachmentFactory>,
        ): MessagePreviewFormatter {
            return DefaultMessagePreviewFormatter(
                context = context,
                autoTranslationEnabled = autoTranslationEnabled,
                messageTextStyle = typography.bodyBold,
                senderNameTextStyle = typography.bodyBold,
                attachmentTextFontStyle = typography.bodyItalic,
                attachmentFactories = attachmentFactories,
            )
        }
    }
}

private class DefaultMessagePreviewFormatter(
    private val context: Context,
    private val autoTranslationEnabled: Boolean,
    private val messageTextStyle: TextStyle,
    private val senderNameTextStyle: TextStyle,
    private val attachmentTextFontStyle: TextStyle,
    private val attachmentFactories: List<AttachmentFactory>,
) : MessagePreviewFormatter {

    override fun formatMessagePreview(
        message: Message,
        currentUser: User?,
    ): AnnotatedString {
        val getTranslatedText: (Message, User?) -> String = { message, currentUser ->
            when (autoTranslationEnabled) {
                true -> currentUser?.language?.let { message.getTranslation(it) } ?: message.text
                else -> message.text
            }
        }
        return buildAnnotatedString {
            message.let { message ->
                val translatedText = getTranslatedText(message, currentUser)

                val userLanguage = currentUser?.language.orEmpty()
                val displayedText = when (autoTranslationEnabled) {
                    true -> message.getTranslation(userLanguage).ifEmpty { message.text }
                    else -> message.text
                }.trim()

                if (message.isSystem()) {
                    append(displayedText)
                } else {
                    appendSenderName(
                        message = message,
                        currentUser = currentUser,
                        senderNameTextStyle = senderNameTextStyle,
                    )
                    appendMessageText(
                        messageText = displayedText,
                        messageTextStyle = messageTextStyle,
                    )
                    appendAttachmentText(
                        attachments = message.attachments,
                        attachmentFactories = attachmentFactories,
                        attachmentTextStyle = attachmentTextFontStyle,
                    )
                }
            }
        }
    }

    private fun AnnotatedString.Builder.appendSenderName(
        message: Message,
        currentUser: User?,
        senderNameTextStyle: TextStyle,
    ) {
        val sender = message.getSenderDisplayName(context, currentUser)

        if (sender != null) {
            append("$sender: ")

            addStyle(
                SpanStyle(
                    fontStyle = senderNameTextStyle.fontStyle,
                    fontWeight = senderNameTextStyle.fontWeight,
                    fontFamily = senderNameTextStyle.fontFamily,
                ),
                start = 0,
                end = sender.length,
            )
        }
    }

    private fun AnnotatedString.Builder.appendMessageText(
        messageText: String,
        messageTextStyle: TextStyle,
    ) {
        if (messageText.isNotEmpty()) {
            val startIndex = this.length
            append("$messageText ")

            addStyle(
                SpanStyle(
                    fontStyle = messageTextStyle.fontStyle,
                    fontFamily = messageTextStyle.fontFamily,
                ),
                start = startIndex,
                end = startIndex + messageText.length,
            )
        }
    }

    private fun AnnotatedString.Builder.appendAttachmentText(
        attachments: List<Attachment>,
        attachmentFactories: List<AttachmentFactory>,
        attachmentTextStyle: TextStyle,
    ) {
        if (attachments.isNotEmpty()) {
            attachmentFactories
                .firstOrNull { it.canHandle(attachments) }
                ?.textFormatter
                ?.let { textFormatter ->
                    attachments.mapNotNull { attachment ->
                        textFormatter.invoke(attachment)
                            .let { previewText ->
                                previewText.ifEmpty { null }
                            }
                    }.joinToString()
                }?.let { attachmentText ->
                    val startIndex = this.length
                    append(attachmentText)

                    addStyle(
                        SpanStyle(
                            fontStyle = attachmentTextStyle.fontStyle,
                            fontFamily = attachmentTextStyle.fontFamily,
                        ),
                        start = startIndex,
                        end = startIndex + attachmentText.length,
                    )
                }
        }
    }
}
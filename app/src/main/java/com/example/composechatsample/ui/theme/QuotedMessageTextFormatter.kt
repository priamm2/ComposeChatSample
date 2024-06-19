package com.example.composechatsample.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import com.example.composechatsample.R
import com.example.composechatsample.common.buildAnnotatedMessageText
import com.example.composechatsample.core.isGiphy
import com.example.composechatsample.core.isMine
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User

public fun interface QuotedMessageTextFormatter {

    public fun format(message: Message, replyMessage: Message?, currentUser: User?): AnnotatedString

    public companion object {


        @Composable
        public fun defaultFormatter(
            autoTranslationEnabled: Boolean,
            context: Context = LocalContext.current,
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
            textStyle: (isMine: Boolean) -> TextStyle = defaultTextStyle(typography, colors),
            builder: AnnotatedQuotedMessageTextBuilder? = null,
        ): QuotedMessageTextFormatter {
            return DefaultQuotedMessageTextFormatter(
                context,
                autoTranslationEnabled,
                typography,
                colors,
                textStyle,
                builder,
            )
        }


        @Composable
        public fun defaultFormatter(
            autoTranslationEnabled: Boolean,
            context: Context = LocalContext.current,
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
            ownMessageTheme: MessageTheme = MessageTheme.defaultOwnTheme(typography, colors),
            otherMessageTheme: MessageTheme = MessageTheme.defaultOtherTheme(typography, colors),
            builder: AnnotatedQuotedMessageTextBuilder? = null,
        ): QuotedMessageTextFormatter {
            val textStyle = defaultTextStyle(ownMessageTheme, otherMessageTheme)
            return defaultFormatter(autoTranslationEnabled, context, typography, colors, textStyle, builder)
        }

        @Composable
        private fun defaultTextStyle(typography: StreamTypography, colors: StreamColors): (Boolean) -> TextStyle {
            val ownTheme = MessageTheme.defaultOwnTheme(typography, colors)
            val otherTheme = MessageTheme.defaultOtherTheme(typography, colors)
            return defaultTextStyle(ownTheme, otherTheme)
        }

        @Composable
        private fun defaultTextStyle(ownTheme: MessageTheme, otherTheme: MessageTheme): (Boolean) -> TextStyle {
            return { isMine ->
                when (isMine) {
                    true -> ownTheme.textStyle
                    else -> otherTheme.textStyle
                }
            }
        }

        public fun composite(vararg formatters: QuotedMessageTextFormatter): QuotedMessageTextFormatter {
            return CompositeQuotedMessageTextFormatter(formatters.toList())
        }
    }
}

public typealias AnnotatedQuotedMessageTextBuilder = AnnotatedString.Builder.(
    message: Message,
    replyMessage: Message?,
    currentUser: User?,
) -> Unit

private class CompositeQuotedMessageTextFormatter(
    private val formatters: List<QuotedMessageTextFormatter>,
) : QuotedMessageTextFormatter {

    override fun format(message: Message, replyMessage: Message?, currentUser: User?): AnnotatedString {
        val builder = AnnotatedString.Builder(message.text)
        for (formatter in formatters) {
            builder.merge(formatter.format(message, replyMessage, currentUser))
        }
        return builder.toAnnotatedString()
    }
}

private class DefaultQuotedMessageTextFormatter(
    private val context: Context,
    private val autoTranslationEnabled: Boolean,
    private val typography: StreamTypography,
    private val colors: StreamColors,
    private val textStyle: (isMine: Boolean) -> TextStyle,
    private val builder: AnnotatedQuotedMessageTextBuilder? = null,
) : QuotedMessageTextFormatter {

    override fun format(message: Message, replyMessage: Message?, currentUser: User?): AnnotatedString {
        val displayedText = when (autoTranslationEnabled) {
            true -> currentUser?.language?.let { userLanguage ->
                message.getTranslation(userLanguage).ifEmpty { message.text }
            } ?: message.text

            else -> message.text
        }

        val attachment = message.attachments.firstOrNull()
        val quotedMessageText = when {
            displayedText.isNotBlank() -> displayedText
            attachment != null -> when {
                attachment.name != null -> attachment.name
                attachment.text != null -> attachment.text
                attachment.isImage() -> context.getString(R.string.stream_compose_quoted_message_image_tag)
                attachment.isGiphy() -> context.getString(R.string.stream_compose_quoted_message_giphy_tag)
                attachment.isAnyFileType() -> context.getString(R.string.stream_compose_quoted_message_file_tag)
                else -> displayedText
            }

            else -> displayedText
        }

        checkNotNull(quotedMessageText) {
            "quotedMessageText is null. Cannot display invalid message title."
        }

        val textColor = textStyle(replyMessage?.isMine(currentUser) != false).color
        return buildAnnotatedMessageText(
            text = displayedText,
            textColor = textColor,
            textFontStyle = typography.body.fontStyle,
            linkColor = colors.primaryAccent,
            builder = {
                builder?.invoke(this, message, replyMessage, currentUser)
            },
        )
    }
}
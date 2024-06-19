package com.example.composechatsample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import com.example.composechatsample.common.buildAnnotatedMessageText
import com.example.composechatsample.core.isMine
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User

fun interface MessageTextFormatter {

    fun format(message: Message, currentUser: User?): AnnotatedString

    companion object {

        @Composable
        public fun defaultFormatter(
            autoTranslationEnabled: Boolean,
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
            textStyle: (isMine: Boolean) -> TextStyle = defaultTextStyle(typography, colors),
            builder: AnnotatedMessageTextBuilder? = null,
        ): MessageTextFormatter {
            return DefaultMessageTextFormatter(autoTranslationEnabled, typography, colors, textStyle, builder)
        }

        @Composable
        public fun defaultFormatter(
            autoTranslationEnabled: Boolean,
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
            ownMessageTheme: MessageTheme = MessageTheme.defaultOwnTheme(typography, colors),
            otherMessageTheme: MessageTheme = MessageTheme.defaultOtherTheme(typography, colors),
            builder: AnnotatedMessageTextBuilder? = null,
        ): MessageTextFormatter {
            val textStyle = defaultTextStyle(ownMessageTheme, otherMessageTheme)
            return defaultFormatter(autoTranslationEnabled, typography, colors, textStyle, builder)
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

        public fun composite(vararg formatters: MessageTextFormatter): MessageTextFormatter {
            return CompositeMessageTextFormatter(formatters.toList())
        }
    }
}

public typealias AnnotatedMessageTextBuilder = AnnotatedString.Builder.(message: Message, currentUser: User?) -> Unit

private class CompositeMessageTextFormatter(
    private val formatters: List<MessageTextFormatter>,
) : MessageTextFormatter {

    override fun format(message: Message, currentUser: User?): AnnotatedString {
        val builder = AnnotatedString.Builder(message.text)
        for (formatter in formatters) {
            builder.merge(formatter.format(message, currentUser))
        }
        return builder.toAnnotatedString()
    }
}

private class DefaultMessageTextFormatter(
    private val autoTranslationEnabled: Boolean,
    private val typography: StreamTypography,
    private val colors: StreamColors,
    private val textStyle: (isMine: Boolean) -> TextStyle,
    private val builder: AnnotatedMessageTextBuilder? = null,
) : MessageTextFormatter {

    override fun format(message: Message, currentUser: User?): AnnotatedString {
        val displayedText = when (autoTranslationEnabled) {
            true -> currentUser?.language?.let { userLanguage ->
                message.getTranslation(userLanguage).ifEmpty { message.text }
            } ?: message.text
            else -> message.text
        }
        val textColor = textStyle(message.isMine(currentUser)).color
        return buildAnnotatedMessageText(
            text = displayedText,
            textColor = textColor,
            textFontStyle = typography.body.fontStyle,
            linkColor = colors.primaryAccent,
            builder = {
                builder?.invoke(this, message, currentUser)
            },
        )
    }
}
package com.example.composechatsample.ui.theme

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User

public fun interface SearchResultNameFormatter {


    public fun formatMessageTitle(message: Message, currentUser: User?): AnnotatedString

    public companion object {
        public fun defaultFormatter(): SearchResultNameFormatter {
            return DefaultSearchResultNameFormatter
        }
    }
}

private object DefaultSearchResultNameFormatter : SearchResultNameFormatter {
    override fun formatMessageTitle(message: Message, currentUser: User?): AnnotatedString =
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(message.user.name)
            }
            message.channelInfo
                ?.takeIf { it.memberCount > 2 }
                ?.name
                ?.let {
                    append(" in ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(it)
                    }
                }
        }
}
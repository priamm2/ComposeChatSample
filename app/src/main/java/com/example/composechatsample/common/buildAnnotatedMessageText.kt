package com.example.composechatsample.common

import android.annotation.SuppressLint
import android.text.util.Linkify
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import java.util.regex.Pattern
import androidx.core.util.PatternsCompat
import java.util.Locale

internal typealias AnnotationTag = String

internal const val AnnotationTagUrl: AnnotationTag = "URL"

internal const val AnnotationTagEmail: AnnotationTag = "EMAIL"

private val URL_SCHEMES = listOf("http://", "https://")
private val EMAIL_SCHEMES = listOf("mailto:")

@SuppressLint("RestrictedApi")
fun buildAnnotatedMessageText(
    text: String,
    textColor: Color,
    textFontStyle: FontStyle?,
    linkColor: Color,
    builder: (AnnotatedString.Builder).() -> Unit = {},
): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        addStyle(
            SpanStyle(
                fontStyle = textFontStyle,
                color = textColor,
            ),
            start = 0,
            end = text.length,
        )
        linkify(
            text = text,
            tag = AnnotationTagUrl,
            pattern = PatternsCompat.AUTOLINK_WEB_URL,
            matchFilter = Linkify.sUrlMatchFilter,
            schemes = URL_SCHEMES,
            linkColor = linkColor,
        )
        linkify(
            text = text,
            tag = AnnotationTagEmail,
            pattern = PatternsCompat.AUTOLINK_EMAIL_ADDRESS,
            schemes = EMAIL_SCHEMES,
            linkColor = linkColor,
        )

        builder(this)
    }
}

private fun AnnotatedString.Builder.linkify(
    text: CharSequence,
    tag: String,
    pattern: Pattern,
    matchFilter: Linkify.MatchFilter? = null,
    schemes: List<String>,
    linkColor: Color,
) {
    @SuppressLint("RestrictedApi")
    val matcher = pattern.matcher(text)
    while (matcher.find()) {
        val start = matcher.start()
        val end = matcher.end()

        if (matchFilter != null && !matchFilter.acceptMatch(text, start, end)) {
            continue
        }

        addStyle(
            style = SpanStyle(
                color = linkColor,
                textDecoration = TextDecoration.Underline,
            ),
            start = start,
            end = end,
        )

        val linkText = requireNotNull(matcher.group(0)!!)

        val url = linkText.fixPrefix(schemes)

        addStringAnnotation(
            tag = tag,
            annotation = url,
            start = start,
            end = end,
        )
    }
}

fun String.fixPrefix(schemes: List<String>): String =
    lowercase(Locale.getDefault())
        .let {
            if (schemes.none { scheme -> it.startsWith(scheme) }) {
                schemes[0] + it
            } else {
                it
            }
        }
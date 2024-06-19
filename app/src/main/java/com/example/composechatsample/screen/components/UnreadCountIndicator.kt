package com.example.composechatsample.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun UnreadCountIndicator(
    unreadCount: Int,
    modifier: Modifier = Modifier,
    color: Color = ChatTheme.colors.errorAccent,
) {
    val displayText = if (unreadCount > LimitTooManyUnreadCount) UnreadCountMany else unreadCount.toString()
    val shape = RoundedCornerShape(9.dp)

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
            .background(shape = shape, color = color)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = displayText,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = ChatTheme.typography.captionBold,
        )
    }
}

private const val UnreadCountMany = "99+"
private const val LimitTooManyUnreadCount = 99

@Preview(showBackground = true, name = "UnreadCountIndicator Preview (Few unread messages)")
@Composable
private fun FewMessagesUnreadCountIndicatorPreview() {
    ChatTheme {
        UnreadCountIndicator(unreadCount = 5)
    }
}

@Preview(showBackground = true, name = "UnreadCountIndicator Preview (Many unread messages)")
@Composable
private fun ManyMessagesUnreadCountIndicatorPreview() {
    ChatTheme {
        UnreadCountIndicator(unreadCount = 200)
    }
}
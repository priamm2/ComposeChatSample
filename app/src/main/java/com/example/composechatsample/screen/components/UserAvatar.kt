package com.example.composechatsample.screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.composechatsample.core.models.User
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun UserAvatar(
    user: User,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
    textStyle: TextStyle = ChatTheme.typography.title3Bold,
    contentDescription: String? = null,
    showOnlineIndicator: Boolean = true,
    onlineIndicatorAlignment: OnlineIndicatorAlignment = OnlineIndicatorAlignment.TopEnd,
    initialsAvatarOffset: DpOffset = DpOffset(0.dp, 0.dp),
    onlineIndicator: @Composable BoxScope.() -> Unit = {
        DefaultOnlineIndicator(onlineIndicatorAlignment)
    },
    onClick: (() -> Unit)? = null,
) {
    Box(modifier = modifier) {
        Avatar(
            modifier = Modifier.fillMaxSize(),
            imageUrl = user.image,
            initials = user.initials,
            textStyle = textStyle,
            shape = shape,
            contentDescription = contentDescription,
            onClick = onClick,
            initialsAvatarOffset = initialsAvatarOffset,
        )

        if (showOnlineIndicator && user.online) {
            onlineIndicator()
        }
    }
}

@Composable
internal fun BoxScope.DefaultOnlineIndicator(onlineIndicatorAlignment: OnlineIndicatorAlignment) {
    OnlineIndicator(modifier = Modifier.align(onlineIndicatorAlignment.alignment))
}

@Preview(showBackground = true, name = "UserAvatar Preview (With avatar image)")
@Composable
private fun UserAvatarForUserWithImagePreview() {
    UserAvatarPreview(PreviewUserData.userWithImage)
}

@Preview(showBackground = true, name = "UserAvatar Preview (With online status)")
@Composable
private fun UserAvatarForOnlineUserPreview() {
    UserAvatarPreview(PreviewUserData.userWithOnlineStatus)
}

@Preview(showBackground = true, name = "UserAvatar Preview (Without avatar image)")
@Composable
private fun UserAvatarForUserWithoutImagePreview() {
    UserAvatarPreview(PreviewUserData.userWithoutImage)
}

@Composable
private fun UserAvatarPreview(user: User) {
    ChatTheme {
        UserAvatar(
            modifier = Modifier.size(36.dp),
            user = user,
            showOnlineIndicator = true,
        )
    }
}
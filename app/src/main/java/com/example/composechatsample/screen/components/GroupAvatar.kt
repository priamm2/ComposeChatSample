package com.example.composechatsample.screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import com.example.composechatsample.common.getAvatarPositionOffset
import com.example.composechatsample.core.models.User
import com.example.composechatsample.ui.theme.ChatTheme

private const val DefaultNumberOfAvatars = 4

@Composable
fun GroupAvatar(
    users: List<User>,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
    textStyle: TextStyle = ChatTheme.typography.captionBold,
    onClick: (() -> Unit)? = null,
) {
    val avatarUsers = users.take(DefaultNumberOfAvatars)
    val imageCount = avatarUsers.size

    val clickableModifier: Modifier = if (onClick != null) {
        modifier.clickable(
            onClick = onClick,
            indication = rememberRipple(bounded = false),
            interactionSource = remember { MutableInteractionSource() },
        )
    } else {
        modifier
    }

    Row(clickableModifier.clip(shape)) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxHeight(),
        ) {
            for (imageIndex in 0 until imageCount step 2) {
                if (imageIndex < imageCount) {
                    UserAvatar(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        user = avatarUsers[imageIndex],
                        shape = RectangleShape,
                        textStyle = textStyle,
                        showOnlineIndicator = false,
                        initialsAvatarOffset = getAvatarPositionOffset(
                            dimens = ChatTheme.dimens,
                            userPosition = imageIndex,
                            memberCount = imageCount,
                        ),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxHeight(),
        ) {
            for (imageIndex in 1 until imageCount step 2) {
                if (imageIndex < imageCount) {
                    UserAvatar(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        user = avatarUsers[imageIndex],
                        shape = RectangleShape,
                        textStyle = textStyle,
                        showOnlineIndicator = false,
                        initialsAvatarOffset = getAvatarPositionOffset(
                            dimens = ChatTheme.dimens,
                            userPosition = imageIndex,
                            memberCount = imageCount,
                        ),
                    )
                }
            }
        }
    }
}
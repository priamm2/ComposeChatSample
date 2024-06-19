package com.example.composechatsample.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.composechatsample.common.initialsGradient
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun InitialsAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
    textStyle: TextStyle = ChatTheme.typography.title3Bold,
    avatarOffset: DpOffset = DpOffset(0.dp, 0.dp),
    onClick: (() -> Unit)? = null,
) {
    val clickableModifier: Modifier = if (onClick != null) {
        modifier.clickable(
            onClick = onClick,
            indication = rememberRipple(bounded = false),
            interactionSource = remember { MutableInteractionSource() },
        )
    } else {
        modifier
    }

    val initialsGradient = initialsGradient(initials = initials)

    Box(
        modifier = clickableModifier
            .clip(shape)
            .background(brush = initialsGradient),
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(avatarOffset.x, avatarOffset.y),
            text = initials,
            style = textStyle,
            color = Color.White,
        )
    }
}
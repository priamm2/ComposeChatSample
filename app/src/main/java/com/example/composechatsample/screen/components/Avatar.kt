package com.example.composechatsample.screen.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import com.example.composechatsample.R
import com.example.composechatsample.common.rememberStreamImagePainter
import com.example.composechatsample.core.applyStreamCdnImageResizingIfEnabled
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun Avatar(
    imageUrl: String,
    initials: String,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
    textStyle: TextStyle = ChatTheme.typography.title3Bold,
    placeholderPainter: Painter? = null,
    contentDescription: String? = null,
    initialsAvatarOffset: DpOffset = DpOffset(0.dp, 0.dp),
    onClick: (() -> Unit)? = null,
) {
    if (LocalInspectionMode.current && imageUrl.isNotBlank()) {

        ImageAvatar(
            modifier = modifier,
            shape = shape,
            painter = painterResource(id = R.drawable.stream_compose_preview_avatar),
            contentDescription = contentDescription,
            onClick = onClick,
        )
        return
    }
    if (imageUrl.isBlank()) {
        InitialsAvatar(
            modifier = modifier,
            initials = initials,
            shape = shape,
            textStyle = textStyle,
            onClick = onClick,
            avatarOffset = initialsAvatarOffset,
        )
        return
    }

    val painter = rememberStreamImagePainter(
        data = imageUrl.applyStreamCdnImageResizingIfEnabled(ChatTheme.streamCdnImageResizing),
        placeholderPainter = painterResource(id = R.drawable.stream_compose_preview_avatar),
    )

    if (painter.state is AsyncImagePainter.State.Error) {
        InitialsAvatar(
            modifier = modifier,
            initials = initials,
            shape = shape,
            textStyle = textStyle,
            onClick = onClick,
            avatarOffset = initialsAvatarOffset,
        )
    } else if (painter.state is AsyncImagePainter.State.Loading && placeholderPainter != null) {
        ImageAvatar(
            modifier = modifier,
            shape = shape,
            painter = placeholderPainter,
            contentDescription = contentDescription,
            onClick = onClick,
        )
    } else {
        ImageAvatar(
            modifier = modifier,
            shape = shape,
            painter = painter,
            contentDescription = contentDescription,
            onClick = onClick,
        )
    }
}

@Preview(showBackground = true, name = "Avatar Preview (With image URL)")
@Composable
private fun AvatarWithImageUrlPreview() {
    AvatarPreview(
        imageUrl = "https://sample.com/image.png",
        initials = "JC",
    )
}

@Preview(showBackground = true, name = "Avatar Preview (Without image URL)")
@Composable
private fun AvatarWithoutImageUrlPreview() {
    AvatarPreview(
        imageUrl = "",
        initials = "JC",
    )
}

@Composable
private fun AvatarPreview(
    imageUrl: String,
    initials: String,
) {
    ChatTheme {
        Avatar(
            modifier = Modifier.size(36.dp),
            imageUrl = imageUrl,
            initials = initials,
        )
    }
}
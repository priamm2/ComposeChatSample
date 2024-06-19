package com.example.composechatsample.screen.messages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composechatsample.PreviewUserReactionData
import com.example.composechatsample.core.ChatClient
import com.example.composechatsample.screen.components.UserAvatar
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.ui.theme.isStartAlignment

@Composable
public fun UserReactionItem(
    item: UserReactionItemState,
    modifier: Modifier = Modifier,
) {
    val (user, painter, type) = item

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val isMine = user.id == ChatClient.instance().getCurrentUser()?.id
        val isStartAlignment = ChatTheme.messageOptionsUserReactionAlignment.isStartAlignment(isMine)
        val alignment = if (isStartAlignment) Alignment.BottomStart else Alignment.BottomEnd

        Box(modifier = Modifier.width(64.dp)) {
            UserAvatar(
                user = user,
                showOnlineIndicator = false,
                modifier = Modifier.size(ChatTheme.dimens.userReactionItemAvatarSize),
            )

            Image(
                modifier = Modifier
                    .background(shape = RoundedCornerShape(16.dp), color = ChatTheme.colors.barsBackground)
                    .size(ChatTheme.dimens.userReactionItemIconSize)
                    .padding(4.dp)
                    .align(alignment),
                painter = painter,
                contentDescription = type,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = user.name,
            style = ChatTheme.typography.footnoteBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textHighEmphasis,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
public fun CurrentUserReactionItemPreview() {
    ChatTheme {
        UserReactionItem(item = PreviewUserReactionData.user1Reaction())
    }
}

@Preview
@Composable
public fun OtherUserReactionItemPreview() {
    ChatTheme {
        UserReactionItem(item = PreviewUserReactionData.user2Reaction())
    }
}
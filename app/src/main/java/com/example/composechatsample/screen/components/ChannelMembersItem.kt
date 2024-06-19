package com.example.composechatsample.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.screen.PreviewUserData
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
internal fun ChannelMembersItem(
    member: Member,
    modifier: Modifier = Modifier,
) {
    val memberName = member.user.name

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UserAvatar(
            modifier = Modifier.size(ChatTheme.dimens.selectedChannelMenuUserItemAvatarSize),
            user = member.user,
            contentDescription = memberName,
        )

        Text(
            text = memberName,
            style = ChatTheme.typography.footnoteBold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = ChatTheme.colors.textHighEmphasis,
        )
    }
}

@Preview(showBackground = true, name = "ChannelMembersItem Preview")
@Composable
private fun ChannelMemberItemPreview() {
    ChatTheme {
        ChannelMembersItem(Member(user = PreviewUserData.user1))
    }
}
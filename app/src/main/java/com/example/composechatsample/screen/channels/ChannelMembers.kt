package com.example.composechatsample.screen.channels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composechatsample.core.models.Member
import com.example.composechatsample.screen.PreviewMembersData
import com.example.composechatsample.screen.components.ChannelMembersItem
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun ChannelMembers(
    members: List<Member>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
    ) {
        items(members) { member ->
            ChannelMembersItem(
                modifier = Modifier
                    .width(ChatTheme.dimens.selectedChannelMenuUserItemWidth)
                    .padding(horizontal = ChatTheme.dimens.selectedChannelMenuUserItemHorizontalPadding),
                member = member,
            )
        }
    }
}

@Preview(showBackground = true, name = "ChannelMembers Preview (One member)")
@Composable
private fun OneMemberChannelMembersPreview() {
    ChatTheme {
        ChannelMembers(members = PreviewMembersData.oneMember)
    }
}

@Preview(showBackground = true, name = "ChannelMembers Preview (Many members)")
@Composable
private fun ManyMembersChannelMembersPreview() {
    ChatTheme {
        ChannelMembers(members = PreviewMembersData.manyMembers)
    }
}
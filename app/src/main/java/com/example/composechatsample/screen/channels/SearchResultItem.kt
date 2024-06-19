package com.example.composechatsample.screen.channels

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.core.models.User
import com.example.composechatsample.screen.components.Timestamp
import com.example.composechatsample.screen.components.UserAvatar
import com.example.composechatsample.ui.theme.ChatTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun SearchResultItem(
    searchResultItemState: ItemState.SearchResultItemState,
    currentUser: User?,
    onSearchResultClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable RowScope.(ItemState.SearchResultItemState) -> Unit = {
        DefaultSearchResultItemLeadingContent(
            searchResultItemState = it,
        )
    },
    centerContent: @Composable RowScope.(ItemState.SearchResultItemState) -> Unit = {
        DefaultSearchResultItemCenterContent(
            searchResultItemState = it,
            currentUser = currentUser,
        )
    },
    trailingContent: @Composable RowScope.(ItemState.SearchResultItemState) -> Unit = {
        DefaultSearchResultItemTrailingContent(
            searchResultItemState = it,
        )
    },
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .combinedClickable(
                onClick = { onSearchResultClick(searchResultItemState.message) },
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() },
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent(searchResultItemState)
            centerContent(searchResultItemState)
            trailingContent(searchResultItemState)
        }
    }
}

@Composable
internal fun DefaultSearchResultItemLeadingContent(
    searchResultItemState: ItemState.SearchResultItemState,
) {
    UserAvatar(
        user = searchResultItemState.message.user,
        modifier = Modifier
            .padding(
                start = ChatTheme.dimens.channelItemHorizontalPadding,
                end = 4.dp,
                top = ChatTheme.dimens.channelItemVerticalPadding,
                bottom = ChatTheme.dimens.channelItemVerticalPadding,
            )
            .size(ChatTheme.dimens.channelAvatarSize),
    )
}

@Composable
internal fun RowScope.DefaultSearchResultItemCenterContent(
    searchResultItemState: ItemState.SearchResultItemState,
    currentUser: User?,
) {
    Column(
        modifier = Modifier
            .padding(start = 4.dp, end = 4.dp)
            .weight(1f)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = ChatTheme.searchResultNameFormatter.formatMessageTitle(searchResultItemState.message, currentUser),
            style = ChatTheme.typography.bodyBold,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textHighEmphasis,
        )

        Text(
            text = ChatTheme.messagePreviewFormatter.formatMessagePreview(
                searchResultItemState.message,
                currentUser,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ChatTheme.typography.body,
            color = ChatTheme.colors.textLowEmphasis,
        )
    }
}

@Composable
internal fun RowScope.DefaultSearchResultItemTrailingContent(
    searchResultItemState: ItemState.SearchResultItemState,
) {
    Column(
        modifier = Modifier
            .padding(
                start = 4.dp,
                end = ChatTheme.dimens.channelItemHorizontalPadding,
                top = ChatTheme.dimens.channelItemVerticalPadding,
                bottom = ChatTheme.dimens.channelItemVerticalPadding,
            )
            .wrapContentHeight()
            .align(Alignment.Bottom),
        horizontalAlignment = Alignment.End,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Timestamp(date = searchResultItemState.message.createdAt)
        }
    }
}
package com.example.composechatsample.screen.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.core.models.User
import com.example.composechatsample.screen.components.UserAvatar
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun MentionSuggestionItem(
    user: User,
    onMentionSelected: (User) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable RowScope.(User) -> Unit = {
        DefaultMentionSuggestionItemLeadingContent(user = it)
    },
    centerContent: @Composable RowScope.(User) -> Unit = {
        DefaultMentionSuggestionItemCenterContent(user = it)
    },
    trailingContent: @Composable RowScope.(User) -> Unit = {
        DefaultMentionSuggestionItemTrailingContent()
    },
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(
                onClick = { onMentionSelected(user) },
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() },
            )
            .padding(
                vertical = ChatTheme.dimens.mentionSuggestionItemVerticalPadding,
                horizontal = ChatTheme.dimens.mentionSuggestionItemHorizontalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent(user)

        centerContent(user)

        trailingContent(user)
    }
}

@Composable
internal fun DefaultMentionSuggestionItemLeadingContent(user: User) {
    UserAvatar(
        modifier = Modifier
            .padding(end = 8.dp)
            .size(ChatTheme.dimens.mentionSuggestionItemAvatarSize),
        user = user,
        showOnlineIndicator = true,
    )
}

@Composable
internal fun RowScope.DefaultMentionSuggestionItemCenterContent(user: User) {
    Column(
        modifier = Modifier
            .weight(1f)
            .wrapContentHeight(),
    ) {
        Text(
            text = user.name,
            style = ChatTheme.typography.bodyBold,
            color = ChatTheme.colors.textHighEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "@${user.id}",
            style = ChatTheme.typography.footnote,
            color = ChatTheme.colors.textLowEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun DefaultMentionSuggestionItemTrailingContent() {
    Icon(
        modifier = Modifier
            .padding(start = 8.dp)
            .size(24.dp),
        painter = painterResource(id = R.drawable.stream_compose_ic_mention),
        contentDescription = null,
        tint = ChatTheme.colors.primaryAccent,
    )
}
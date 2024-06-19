package com.example.composechatsample.screen.messages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.core.models.Command
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun CommandSuggestionItem(
    command: Command,
    modifier: Modifier = Modifier,
    onCommandSelected: (Command) -> Unit = {},
    leadingContent: @Composable RowScope.(Command) -> Unit = {
        DefaultCommandSuggestionItemLeadingContent()
    },
    centerContent: @Composable RowScope.(Command) -> Unit = {
        DefaultCommandSuggestionItemCenterContent(command = it)
    },
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(
                onClick = { onCommandSelected(command) },
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() },
            )
            .padding(
                vertical = ChatTheme.dimens.commandSuggestionItemVerticalPadding,
                horizontal = ChatTheme.dimens.commandSuggestionItemHorizontalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent(command)

        centerContent(command)
    }
}

@Composable
internal fun DefaultCommandSuggestionItemLeadingContent() {
    Image(
        modifier = Modifier
            .padding(end = 8.dp)
            .size(ChatTheme.dimens.commandSuggestionItemIconSize),
        painter = painterResource(id = R.drawable.stream_compose_ic_giphy),
        contentDescription = null,
    )
}

@Composable
internal fun RowScope.DefaultCommandSuggestionItemCenterContent(
    command: Command,
    modifier: Modifier = Modifier,
) {
    val commandDescription = LocalContext.current.getString(
        R.string.stream_compose_message_composer_command_template,
        command.name,
        command.args,
    )

    Row(
        modifier = modifier
            .weight(1f)
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = command.name.replaceFirstChar(Char::uppercase),
            style = ChatTheme.typography.bodyBold,
            color = ChatTheme.colors.textHighEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = commandDescription,
            style = ChatTheme.typography.body,
            color = ChatTheme.colors.textLowEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
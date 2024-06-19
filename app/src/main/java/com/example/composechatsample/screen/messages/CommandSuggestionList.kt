package com.example.composechatsample.screen.messages

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.core.models.Command
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
fun CommandSuggestionList(
    commands: List<Command>,
    modifier: Modifier = Modifier,
    onCommandSelected: (Command) -> Unit = {},
    itemContent: @Composable (Command) -> Unit = { command ->
        DefaultCommandSuggestionItem(
            command = command,
            onCommandSelected = onCommandSelected,
        )
    },
) {
    SuggestionList(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = ChatTheme.dimens.suggestionListMaxHeight)
            .padding(ChatTheme.dimens.suggestionListPadding),
        headerContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(24.dp),
                    painter = painterResource(id = R.drawable.stream_compose_ic_command),
                    tint = ChatTheme.colors.primaryAccent,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(id = R.string.stream_compose_message_composer_instant_commands),
                    style = ChatTheme.typography.body,
                    maxLines = 1,
                    color = ChatTheme.colors.textLowEmphasis,
                )
            }
        },
    ) {
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            items(
                items = commands,
                key = Command::name,
            ) { command ->
                itemContent(command)
            }
        }
    }
}

@Composable
internal fun DefaultCommandSuggestionItem(
    command: Command,
    onCommandSelected: (Command) -> Unit,
) {
    CommandSuggestionItem(command = command, onCommandSelected = onCommandSelected)
}
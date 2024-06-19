package com.example.composechatsample.screen.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun ModeratedMessageOptionItem(
    option: ModeratedMessageOption,
    modifier: Modifier = Modifier,
) {
    Divider(color = ChatTheme.colors.borders)
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = option.text),
            style = ChatTheme.typography.body,
            color = ChatTheme.colors.primaryAccent,
        )
    }
}
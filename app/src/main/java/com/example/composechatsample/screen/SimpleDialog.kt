package com.example.composechatsample.screen

import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.composechatsample.R
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun SimpleDialog(
    title: String,
    message: String,
    onPositiveAction: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = ChatTheme.colors.textHighEmphasis,
                style = ChatTheme.typography.title3Bold,
            )
        },
        text = {
            Text(
                text = message,
                color = ChatTheme.colors.textHighEmphasis,
                style = ChatTheme.typography.body,
            )
        },
        confirmButton = {
            TextButton(
                colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.primaryAccent),
                onClick = { onPositiveAction() },
            ) {
                Text(text = stringResource(id = R.string.stream_compose_ok))
            }
        },
        dismissButton = {
            TextButton(
                colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.primaryAccent),
                onClick = onDismiss,
            ) {
                Text(text = stringResource(id = R.string.stream_compose_cancel))
            }
        },
        backgroundColor = ChatTheme.colors.barsBackground,
    )
}
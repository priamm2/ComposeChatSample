package com.example.composechatsample.screen.messages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.composechatsample.R
import com.example.composechatsample.core.models.Message
import com.example.composechatsample.ui.theme.ChatTheme

@Composable
public fun ModeratedMessageDialog(
    message: Message,
    onDismissRequest: () -> Unit,
    onDialogOptionInteraction: (message: Message, option: ModeratedMessageOption) -> Unit,
    modifier: Modifier = Modifier,
    moderatedMessageOptions: List<ModeratedMessageOption> = defaultMessageModerationOptions(),
    dialogTitle: @Composable () -> Unit = { DefaultModeratedMessageDialogTitle() },
    dialogDescription: @Composable () -> Unit = { DefaultModeratedMessageDialogDescription() },
    dialogOptions: @Composable () -> Unit = {
        DefaultModeratedDialogOptions(
            message = message,
            moderatedMessageOptions = moderatedMessageOptions,
            onDialogOptionInteraction = onDialogOptionInteraction,
            onDismissRequest = onDismissRequest,
        )
    },
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            dialogTitle()

            dialogDescription()

            dialogOptions()
        }
    }
}

@Composable
internal fun DefaultModeratedDialogOptions(
    message: Message,
    moderatedMessageOptions: List<ModeratedMessageOption>,
    onDialogOptionInteraction: (message: Message, option: ModeratedMessageOption) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Spacer(modifier = Modifier.height(12.dp))

    ModeratedMessageDialogOptions(
        message = message,
        options = moderatedMessageOptions,
        onDismissRequest = onDismissRequest,
        onDialogOptionInteraction = onDialogOptionInteraction,
    )
}

@Composable
internal fun DefaultModeratedMessageDialogTitle() {
    Spacer(modifier = Modifier.height(12.dp))

    val painter = painterResource(id = R.drawable.stream_compose_ic_flag)
    Image(
        painter = painter,
        contentDescription = "",
        colorFilter = ColorFilter.tint(ChatTheme.colors.primaryAccent),
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = stringResource(id = R.string.stream_ui_moderation_dialog_title),
        textAlign = TextAlign.Center,
        style = ChatTheme.typography.title3,
        color = ChatTheme.colors.textHighEmphasis,
    )
}

@Composable
internal fun DefaultModeratedMessageDialogDescription() {
    Spacer(modifier = Modifier.height(12.dp))

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        text = stringResource(id = R.string.stream_ui_moderation_dialog_description),
        textAlign = TextAlign.Center,
        style = ChatTheme.typography.body,
        color = ChatTheme.colors.textLowEmphasis,
    )
}
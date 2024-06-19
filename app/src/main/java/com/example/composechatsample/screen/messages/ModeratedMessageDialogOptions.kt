package com.example.composechatsample.screen.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.composechatsample.core.models.Message

@Composable
public fun ModeratedMessageDialogOptions(
    message: Message,
    options: List<ModeratedMessageOption>,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    onDialogOptionInteraction: (message: Message, option: ModeratedMessageOption) -> Unit = { _, _ -> },
    itemContent: @Composable (ModeratedMessageOption) -> Unit = { option ->
        DefaultModeratedMessageOptionItem(message, option, onDismissRequest, onDialogOptionInteraction)
    },
) {
    LazyColumn(modifier = modifier) {
        items(options) { option ->
            itemContent(option)
        }
    }
}

@Composable
internal fun DefaultModeratedMessageOptionItem(
    message: Message,
    option: ModeratedMessageOption,
    onDismissRequest: () -> Unit,
    onDialogOptionInteraction: (message: Message, option: ModeratedMessageOption) -> Unit,
) {
    ModeratedMessageOptionItem(
        option = option,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
            ) {
                onDialogOptionInteraction(message, option)
                onDismissRequest()
            },
    )
}
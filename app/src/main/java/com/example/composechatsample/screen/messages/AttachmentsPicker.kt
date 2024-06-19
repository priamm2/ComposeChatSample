package com.example.composechatsample.screen.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.composechatsample.R
import com.example.composechatsample.common.mirrorRtl
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.ui.theme.AttachmentsPickerTabFactory
import com.example.composechatsample.ui.theme.ChatTheme
import com.example.composechatsample.viewModel.AttachmentsPickerViewModel

@Composable
public fun AttachmentsPicker(
    attachmentsPickerViewModel: AttachmentsPickerViewModel,
    onAttachmentsSelected: (List<Attachment>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    tabFactories: List<AttachmentsPickerTabFactory> = ChatTheme.attachmentsPickerTabFactories,
    shape: Shape = ChatTheme.shapes.bottomSheet,
) {
    val defaultTabIndex = tabFactories.indexOfFirst { it.isPickerTabEnabled() }.takeIf { it >= 0 } ?: 0
    var selectedTabIndex by remember { mutableIntStateOf(defaultTabIndex) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.overlay)
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ),
    ) {
        Card(
            modifier = modifier.clickable(
                indication = null,
                onClick = {},
                interactionSource = remember { MutableInteractionSource() },
            ),
            elevation = 4.dp,
            shape = shape,
            backgroundColor = ChatTheme.colors.inputBackground,
        ) {
            Column {
                AttachmentPickerOptions(
                    hasPickedAttachments = attachmentsPickerViewModel.hasPickedAttachments,
                    tabFactories = tabFactories,
                    tabIndex = selectedTabIndex,
                    onTabClick = { index, attachmentPickerMode ->
                        selectedTabIndex = index
                        attachmentsPickerViewModel.changeAttachmentPickerMode(attachmentPickerMode) { false }
                    },
                    onSendAttachmentsClick = {
                        onAttachmentsSelected(attachmentsPickerViewModel.getSelectedAttachments())
                    },
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    color = ChatTheme.colors.barsBackground,
                ) {
                    tabFactories.getOrNull(selectedTabIndex)
                        ?.PickerTabContent(
                            attachments = attachmentsPickerViewModel.attachments,
                            onAttachmentItemSelected = attachmentsPickerViewModel::changeSelectedAttachments,
                            onAttachmentsChanged = { attachmentsPickerViewModel.attachments = it },
                            onAttachmentsSubmitted = {
                                onAttachmentsSelected(attachmentsPickerViewModel.getAttachmentsFromMetaData(it))
                            },
                        )
                }
            }
        }
    }
}

@Composable
private fun AttachmentPickerOptions(
    hasPickedAttachments: Boolean,
    tabFactories: List<AttachmentsPickerTabFactory>,
    tabIndex: Int,
    onTabClick: (Int, AttachmentsPickerMode) -> Unit,
    onSendAttachmentsClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            tabFactories.forEachIndexed { index, tabFactory ->

                val isSelected = index == tabIndex
                val isEnabled = isSelected || (!hasPickedAttachments && tabFactory.isPickerTabEnabled())

                IconButton(
                    enabled = isEnabled,
                    content = {
                        tabFactory.PickerTabIcon(
                            isEnabled = isEnabled,
                            isSelected = isSelected,
                        )
                    },
                    onClick = { onTabClick(index, tabFactory.attachmentsPickerMode) },
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            enabled = hasPickedAttachments,
            onClick = onSendAttachmentsClick,
            content = {
                val layoutDirection = LocalLayoutDirection.current

                Icon(
                    modifier = Modifier
                        .weight(1f)
                        .mirrorRtl(layoutDirection = layoutDirection),
                    painter = painterResource(id = R.drawable.stream_compose_ic_circle_left),
                    contentDescription = stringResource(id = R.string.stream_compose_send_attachment),
                    tint = if (hasPickedAttachments) {
                        ChatTheme.colors.primaryAccent
                    } else {
                        ChatTheme.colors.textLowEmphasis
                    },
                )
            },
        )
    }
}
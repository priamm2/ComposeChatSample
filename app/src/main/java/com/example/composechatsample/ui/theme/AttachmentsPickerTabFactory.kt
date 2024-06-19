package com.example.composechatsample.ui.theme

import androidx.compose.runtime.Composable
import com.example.composechatsample.screen.messages.AttachmentsPickerMode

public interface AttachmentsPickerTabFactory {

    public val attachmentsPickerMode: AttachmentsPickerMode

    public fun isPickerTabEnabled(): Boolean = true

    @Composable
    public fun PickerTabIcon(isEnabled: Boolean, isSelected: Boolean)

    @Composable
    public fun PickerTabContent(
        attachments: List<AttachmentPickerItemState>,
        onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
        onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
        onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
    )
}
package com.example.composechatsample.viewModel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.composechatsample.common.AttachmentMetaData
import com.example.composechatsample.common.AttachmentPickerItemState
import com.example.composechatsample.core.models.Attachment
import com.example.composechatsample.screen.messages.AttachmentsPickerMode
import com.example.composechatsample.screen.messages.Files
import com.example.composechatsample.screen.messages.Images

public class AttachmentsPickerViewModel(
    private val storageHelper: StorageHelperWrapper,
) : ViewModel() {

    public var attachmentsPickerMode: AttachmentsPickerMode by mutableStateOf(Images)
        private set

    public var images: List<AttachmentPickerItemState> by mutableStateOf(emptyList())

    public var files: List<AttachmentPickerItemState> by mutableStateOf(emptyList())

    public var attachments: List<AttachmentPickerItemState> by mutableStateOf(emptyList())

    public val hasPickedFiles: Boolean
        get() = files.any { it.isSelected }

    public val hasPickedImages: Boolean
        get() = images.any { it.isSelected }

    public val hasPickedAttachments: Boolean
        get() = attachments.any { it.isSelected }

    public var isShowingAttachments: Boolean by mutableStateOf(false)
        private set

    public fun loadData() {
        loadAttachmentsData(attachmentsPickerMode)
    }

    public fun changeAttachmentPickerMode(
        attachmentsPickerMode: AttachmentsPickerMode,
        hasPermission: () -> Boolean = { true },
    ) {
        this.attachmentsPickerMode = attachmentsPickerMode

        if (hasPermission()) loadAttachmentsData(attachmentsPickerMode)
    }

    public fun changeAttachmentState(showAttachments: Boolean) {
        isShowingAttachments = showAttachments

        if (!showAttachments) {
            dismissAttachments()
        }
    }

    private fun loadAttachmentsData(attachmentsPickerMode: AttachmentsPickerMode) {
        if (attachmentsPickerMode == Images) {
            val images = storageHelper.getMedia().map { AttachmentPickerItemState(it, false) }
            this.images = images
            this.attachments = images
            this.files = emptyList()
        } else if (attachmentsPickerMode == Files) {
            val files = storageHelper.getFiles().map { AttachmentPickerItemState(it, false) }
            this.files = files
            this.attachments = files
            this.images = emptyList()
        }
    }

    public fun changeSelectedAttachments(attachmentItem: AttachmentPickerItemState) {
        val dataSet = attachments

        val itemIndex = dataSet.indexOf(attachmentItem)
        val newFiles = dataSet.toMutableList()

        val newItem = dataSet[itemIndex].copy(isSelected = !newFiles[itemIndex].isSelected)

        newFiles.removeAt(itemIndex)
        newFiles.add(itemIndex, newItem)

        if (attachmentsPickerMode == Files) {
            files = newFiles
        } else if (attachmentsPickerMode == Images) {
            images = newFiles
        }
        attachments = newFiles
    }

    public fun getSelectedAttachments(): List<Attachment> {
        val dataSet = if (attachmentsPickerMode == Files) files else images
        val selectedAttachments = dataSet.filter { it.isSelected }

        return storageHelper.getAttachmentsForUpload(selectedAttachments.map { it.attachmentMetaData })
    }

    public fun getAttachmentsFromUris(uris: List<Uri>): List<Attachment> {
        return storageHelper.getAttachmentsFromUris(uris)
    }

    public fun getAttachmentsFromMetaData(metaData: List<AttachmentMetaData>): List<Attachment> {
        return storageHelper.getAttachmentsForUpload(metaData)
    }

    public fun dismissAttachments() {
        attachmentsPickerMode = Images
        images = emptyList()
        files = emptyList()
    }
}
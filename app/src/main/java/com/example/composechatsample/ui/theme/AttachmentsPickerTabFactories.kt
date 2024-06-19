package com.example.composechatsample.ui.theme

import com.example.composechatsample.common.AttachmentsPickerImagesTabFactory

public object AttachmentsPickerTabFactories {

    public fun defaultFactories(
        imagesTabEnabled: Boolean = true,
        filesTabEnabled: Boolean = true,
        takeImageEnabled: Boolean = true,
        recordVideoEnabled: Boolean = true,
    ): List<AttachmentsPickerTabFactory> {
        return listOfNotNull(
            if (imagesTabEnabled) AttachmentsPickerImagesTabFactory() else null,
            if (filesTabEnabled) AttachmentsPickerFilesTabFactory() else null,
            when {
                takeImageEnabled && recordVideoEnabled ->
                    AttachmentsPickerMediaCaptureTabFactory(
                        AttachmentsPickerMediaCaptureTabFactory.PickerMediaMode.PHOTO_AND_VIDEO,
                    )
                takeImageEnabled ->
                    AttachmentsPickerMediaCaptureTabFactory(
                        AttachmentsPickerMediaCaptureTabFactory.PickerMediaMode.PHOTO,
                    )
                recordVideoEnabled ->
                    AttachmentsPickerMediaCaptureTabFactory(
                        AttachmentsPickerMediaCaptureTabFactory.PickerMediaMode.VIDEO,
                    )
                else -> null
            },
        )
    }
}
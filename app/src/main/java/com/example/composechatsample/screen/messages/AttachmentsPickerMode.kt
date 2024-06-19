package com.example.composechatsample.screen.messages

public sealed class AttachmentsPickerMode

public object Images : AttachmentsPickerMode() { override fun toString(): String = "Images" }

public object Files : AttachmentsPickerMode() { override fun toString(): String = "Files" }

public object MediaCapture : AttachmentsPickerMode() { override fun toString(): String = "MediaCapture" }

public data class CustomPickerMode(
    public val extraProperties: Map<String, Any> = emptyMap(),
) : AttachmentsPickerMode()
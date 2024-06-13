package com.example.composechatsample.core

import com.example.composechatsample.core.models.Attachment

public data class RecordedMedia(
    val durationInMs: Int,
    val attachment: Attachment,
)
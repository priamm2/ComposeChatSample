package com.example.composechatsample.common

import com.example.composechatsample.core.models.Attachment

val Attachment.duration: Float?
    get() = (extraData[EXTRA_DURATION] as? Number)?.toFloat()
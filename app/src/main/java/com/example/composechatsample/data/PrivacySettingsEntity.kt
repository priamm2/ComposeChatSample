package com.example.composechatsample.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PrivacySettingsEntity(
    val typingIndicators: TypingIndicatorsEntity? = null,
    val readReceipts: ReadReceiptsEntity? = null,
)

@JsonClass(generateAdapter = true)
data class TypingIndicatorsEntity(
    val enabled: Boolean,
)

@JsonClass(generateAdapter = true)
data class ReadReceiptsEntity(
    val enabled: Boolean,
)
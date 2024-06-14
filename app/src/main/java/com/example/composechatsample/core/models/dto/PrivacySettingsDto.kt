package com.example.composechatsample.core.models.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PrivacySettingsDto(
    val typing_indicators: TypingIndicatorsDto? = null,
    val read_receipts: ReadReceiptsDto? = null,
)
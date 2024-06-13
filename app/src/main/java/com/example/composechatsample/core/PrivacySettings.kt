package com.example.composechatsample.core

import androidx.compose.runtime.Immutable

@Immutable
data class PrivacySettings(
    val typingIndicators: TypingIndicators? = null,
    val readReceipts: ReadReceipts? = null,
)

@Immutable
data class TypingIndicators(
    val enabled: Boolean = true,
)

@Immutable
data class ReadReceipts(
    val enabled: Boolean = true,
)

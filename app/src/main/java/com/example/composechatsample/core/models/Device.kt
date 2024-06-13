package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable

@Immutable
public data class Device(
    val token: String,
    val pushProvider: PushProvider,
    val providerName: String?,
)

public enum class PushProvider(public val key: String) {
    FIREBASE("firebase"),
    HUAWEI("huawei"),
    XIAOMI("xiaomi"),
    UNKNOWN("unknown"),
    ;

    public companion object {
        public fun fromKey(key: String): PushProvider =
            values().firstOrNull { it.key == key } ?: UNKNOWN
    }
}

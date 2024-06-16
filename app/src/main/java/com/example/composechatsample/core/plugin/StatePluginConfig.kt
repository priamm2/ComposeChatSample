package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.TimeDuration

data class StatePluginConfig @JvmOverloads constructor(
    val backgroundSyncEnabled: Boolean = true,
    val userPresence: Boolean = true,
    val syncMaxThreshold: TimeDuration = TimeDuration.hours(12),
)
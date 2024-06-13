package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.TimeDuration

public data class StatePluginConfig @JvmOverloads constructor(
    public val backgroundSyncEnabled: Boolean = true,
    public val userPresence: Boolean = true,
    public val syncMaxThreshold: TimeDuration = TimeDuration.hours(12),
)
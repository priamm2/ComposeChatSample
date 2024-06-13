package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable
import com.example.composechatsample.core.models.Config

@Immutable
public data class ChannelConfig(val type: String, val config: Config)

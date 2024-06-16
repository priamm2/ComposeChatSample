package com.example.composechatsample.core.push

import com.example.composechatsample.core.models.PushProvider

data class PushDevice(
    val token: String,
    val pushProvider: PushProvider,
    val providerName: String,
)
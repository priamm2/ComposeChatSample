package com.example.composechatsample.core.push

import android.content.Context

public abstract class PushDelegate(public val context: Context) {

    public abstract fun handlePushMessage(
        metadata: Map<String, Any?>,
        payload: Map<String, Any?>,
    ): Boolean

    public abstract fun registerPushDevice(pushDevice: PushDevice)
}
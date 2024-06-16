package com.example.composechatsample.core.push

import android.content.Context

public interface PushDeviceGenerator {
    /**
     * Checks if push notification provider is valid for this device
     */
    public fun isValidForThisDevice(context: Context): Boolean

    /**
     * Called when this [PushDeviceGenerator] has been selected to be used.
     */
    public fun onPushDeviceGeneratorSelected()

    /**
     * Asynchronously generates a [PushDevice] and calls [onPushDeviceGenerated] callback once it's ready
     */
    public fun asyncGeneratePushDevice(onPushDeviceGenerated: (pushDevice: PushDevice) -> Unit)
}
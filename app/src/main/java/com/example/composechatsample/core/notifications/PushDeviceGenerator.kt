package com.example.composechatsample.core.notifications

import android.content.Context
import com.example.composechatsample.core.models.Device

public interface PushDeviceGenerator {

    public fun isValidForThisDevice(context: Context): Boolean

    public fun onPushDeviceGeneratorSelected()

    public fun asyncGenerateDevice(onDeviceGenerated: (device: Device) -> Unit)
}
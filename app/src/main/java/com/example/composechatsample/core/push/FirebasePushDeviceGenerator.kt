package com.example.composechatsample.core.push

import android.content.Context
import com.example.composechatsample.core.models.PushProvider
import com.example.composechatsample.log.StreamLog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging


class FirebasePushDeviceGenerator
@JvmOverloads
constructor(
    private val firebaseMessaging: FirebaseMessaging = FirebaseMessaging.getInstance(),
    private val providerName: String,
) : PushDeviceGenerator {
    private val logger = StreamLog.getLogger("Push:Firebase")

    override fun isValidForThisDevice(context: Context): Boolean =
        (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS).also {
            logger.i { "Is Firebase available on on this device -> $it" }
        }

    override fun onPushDeviceGeneratorSelected() {
        FirebaseMessagingDelegate.fallbackProviderName = providerName
    }

    override fun asyncGeneratePushDevice(onPushDeviceGenerated: (pushDevice: PushDevice) -> Unit) {
        logger.i { "Getting Firebase token" }
        firebaseMessaging.token.addOnCompleteListener {
            if (it.isSuccessful) {
                logger.i { "Firebase returned token successfully" }
                onPushDeviceGenerated(
                    PushDevice(
                        token = it.result,
                        pushProvider = PushProvider.FIREBASE,
                        providerName = providerName,
                    ),
                )
            } else {
                logger.i { "Error: Firebase didn't returned token" }
            }
        }
    }
}
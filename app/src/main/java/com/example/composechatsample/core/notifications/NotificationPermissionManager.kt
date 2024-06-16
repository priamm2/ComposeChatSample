package com.example.composechatsample.core.notifications

import android.app.Application
import com.example.composechatsample.core.push.PushNotificationPermissionRequester
import com.example.composechatsample.log.taggedLogger

public class NotificationPermissionManager private constructor(
    private val pushNotificationPermissionRequester: PushNotificationPermissionRequester,
    private val requestPermissionOnAppLaunch: () -> Boolean,
    private val onPermissionStatus: (NotificationPermissionStatus) -> Unit,
) : PushNotificationPermissionRequester.PushNotificationPermissionCallback {
    private val logger by taggedLogger("Push:Notifications-PM")
    private var started = false

    private fun initialize() {
        logger.d { "[initialize] no args" }
        pushNotificationPermissionRequester.addCallback(this)
    }

    public fun start() {
        logger.d { "[start] no args" }
        requestPermission()
    }

    public fun stop() {
        logger.d { "[stop] no args" }
        started = false
    }

    override fun onAppLaunched() {
        logger.d { "[onAppLaunched] no args" }
        if (requestPermissionOnAppLaunch()) {
            requestPermission()
        }
    }

    override fun onPermissionStatusChanged(status: NotificationPermissionStatus) {
        onPermissionStatus(status)
    }

    private fun requestPermission() {
        if (!started) {
            pushNotificationPermissionRequester.requestPermission()
        }
        started = true
    }

    public companion object {
        public fun createNotificationPermissionsManager(
            application: Application,
            requestPermissionOnAppLaunch: () -> Boolean,
            onPermissionStatus: (NotificationPermissionStatus) -> Unit,
        ): NotificationPermissionManager =
            NotificationPermissionManager(
                PushNotificationPermissionRequester.getInstance(application),
                requestPermissionOnAppLaunch,
                onPermissionStatus,
            ).also { it.initialize() }
    }
}
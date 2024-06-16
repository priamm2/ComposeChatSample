package com.example.composechatsample.core.notifications

import android.app.Activity
import android.app.Application
import android.widget.Toast
import com.example.composechatsample.R

import com.example.composechatsample.log.taggedLogger

public class DefaultNotificationPermissionHandler private constructor() : NotificationPermissionHandler,
    ActivityLifecycleCallbacks() {
    private val logger by taggedLogger("Push:Default-NPH")

    private var currentActivity: Activity? = null

    override fun onActivityStarted(activity: Activity) {
        super.onActivityStarted(activity)
        currentActivity = activity
    }

    override fun onLastActivityStopped(activity: Activity) {
        super.onLastActivityStopped(activity)
        currentActivity = null
    }

    override fun onPermissionRequested() { /* no-op */ }

    override fun onPermissionGranted() { /* no-op */ }

    override fun onPermissionDenied() {
        logger.i { "[onPermissionDenied] currentActivity: $currentActivity" }
        currentActivity?.showNotificationBlocked()
    }

    override fun onPermissionRationale() { /* no-op */ }

    private fun Activity.showNotificationBlocked() {
        Toast.makeText(
            this,
            R.string.stream_push_permissions_notifications_message,
            Toast.LENGTH_LONG,
        ).show()
    }

    public companion object {
        public fun createDefaultNotificationPermissionHandler(
            application: Application,
        ): DefaultNotificationPermissionHandler =
            DefaultNotificationPermissionHandler()
                .also { application.registerActivityLifecycleCallbacks(it) }
    }
}
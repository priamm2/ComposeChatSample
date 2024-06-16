package com.example.composechatsample.core.notifications

public interface NotificationPermissionHandler {

    public fun onPermissionRequested()

    /**
     * Invoked when permission is granted.
     */
    public fun onPermissionGranted()

    /**
     * Invoked when permission is denied.
     */
    public fun onPermissionDenied()

    /**
     * Invoked when permission rationale is required.
     */
    public fun onPermissionRationale()
}
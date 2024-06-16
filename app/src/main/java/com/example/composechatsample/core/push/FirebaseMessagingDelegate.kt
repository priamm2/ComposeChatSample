package com.example.composechatsample.core.push

import com.example.composechatsample.core.models.PushProvider
import com.google.firebase.messaging.RemoteMessage

public object FirebaseMessagingDelegate {
    internal var fallbackProviderName: String? = null

    @JvmStatic
    public fun handleRemoteMessage(remoteMessage: RemoteMessage): Boolean {
        return PushDelegateProvider.delegates.any {
            it.handlePushMessage(
                metadata = remoteMessage.extractMetadata(),
                payload = remoteMessage.data,
            )
        }
    }

    @JvmStatic
    public fun registerFirebaseToken(
        token: String,
        providerName: String,
    ) {
        (providerName.takeUnless { it.isBlank() } ?: fallbackProviderName)
            ?.let {
                PushDevice(
                    token = token,
                    pushProvider = PushProvider.FIREBASE,
                    providerName = it,
                )
            }
            ?.let {
                PushDelegateProvider.delegates.forEach { delegate -> delegate.registerPushDevice(it) }
            }
    }

    private fun RemoteMessage.extractMetadata(): Map<String, Any> {
        return hashMapOf<String, Any>().apply {
            senderId?.also { put("firebase.sender_id", it) }
            from?.also { put("firebase.from", it) }
            to?.also { put("firebase.to", it) }
            messageType?.also { put("firebase.message_type", it) }
            messageId?.also { put("firebase.message_id", it) }
            collapseKey?.also { put("firebase.collapse_key", it) }
            put("firebase.sent_time", sentTime)
            put("firebase.ttl", ttl)
            put("firebase.priority", priority)
            put("firebase.priority", originalPriority)
        }
    }
}
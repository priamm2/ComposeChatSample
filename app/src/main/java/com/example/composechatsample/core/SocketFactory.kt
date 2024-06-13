package com.example.composechatsample.core

import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.token.TokenManager
import com.example.composechatsample.log.taggedLogger
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal class SocketFactory(
    private val parser: ChatParser,
    private val tokenManager: TokenManager,
    private val httpClient: OkHttpClient = OkHttpClient(),
) {
    private val logger by taggedLogger("Chat:SocketFactory")

    @Throws(UnsupportedEncodingException::class)
    fun createSocket(connectionConf: ConnectionConf): StreamWebSocket {
        val request = buildRequest(connectionConf)
        logger.i { "new web socket: ${request.url}" }
        return StreamWebSocket(parser) { httpClient.newWebSocket(request, it) }
    }

    @Throws(UnsupportedEncodingException::class)
    private fun buildRequest(connectionConf: ConnectionConf): Request =
        Request.Builder()
            .url(buildUrl(connectionConf))
            .build()

    @Suppress("TooGenericExceptionCaught")
    @Throws(UnsupportedEncodingException::class)
    private fun buildUrl(connectionConf: ConnectionConf): String {
        var json = buildUserDetailJson(connectionConf)
        return try {
            json = URLEncoder.encode(json, StandardCharsets.UTF_8.name())
            val baseWsUrl = "${connectionConf.endpoint}connect?json=$json&api_key=${connectionConf.apiKey}"
            when (connectionConf) {
                is ConnectionConf.AnonymousConnectionConf -> "$baseWsUrl&stream-auth-type=anonymous"
                is ConnectionConf.UserConnectionConf -> {
                    val token = tokenManager.getToken()
                        .takeUnless { connectionConf.isReconnection }
                        ?: tokenManager.loadSync()
                    "$baseWsUrl&authorization=$token&stream-auth-type=jwt"
                }
            }
        } catch (_: UnsupportedEncodingException) {
            throw UnsupportedEncodingException("Unable to encode user details json: $json")
        }
    }

    private fun buildUserDetailJson(connectionConf: ConnectionConf): String {
        val data = mapOf(
            "user_details" to connectionConf.reduceUserDetails(),
            "user_id" to connectionConf.id,
            "server_determines_connection_id" to true,
            "X-Stream-Client" to ChatClient.buildSdkTrackingHeaders(),
        )
        return parser.toJson(data)
    }

    private fun ConnectionConf.reduceUserDetails(): Map<String, Any> = mutableMapOf<String, Any>("id" to id)
        .apply {
            if (!isReconnection) {
                if (user.role.isNotBlank()) put("role", user.role)
                user.banned?.also { put("banned", it) }
                user.invisible?.also { put("invisible", it) }
                user.privacySettings?.also { put("privacy_settings", it.reducePrivacySettings()) }
                if (user.teams.isNotEmpty()) put("teams", user.teams)
                if (user.language.isNotBlank()) put("language", user.language)
                if (user.image.isNotBlank()) put("image", user.image)
                if (user.name.isNotBlank()) put("name", user.name)
                putAll(user.extraData)
            }
        }

    private fun PrivacySettings.reducePrivacySettings(): Map<String, Any> = mutableMapOf<String, Any>()
        .apply {
            typingIndicators?.also {
                put(
                    "typing_indicators",
                    mapOf<String, Any>(
                        "enabled" to it.enabled,
                    ),
                )
            }
            readReceipts?.also {
                put(
                    "read_receipts",
                    mapOf<String, Any>(
                        "enabled" to it.enabled,
                    ),
                )
            }
        }

    internal sealed class ConnectionConf {
        var isReconnection: Boolean = false
            private set
        abstract val endpoint: String
        abstract val apiKey: String
        abstract val user: User

        data class AnonymousConnectionConf(
            override val endpoint: String,
            override val apiKey: String,
            override val user: User,
        ) : ConnectionConf()

        data class UserConnectionConf(
            override val endpoint: String,
            override val apiKey: String,
            override val user: User,
        ) : ConnectionConf()

        internal fun asReconnectionConf(): ConnectionConf = this.also { isReconnection = true }

        internal val id: String
            get() = when (this) {
                is AnonymousConnectionConf -> user.id.replace("!", "")
                is UserConnectionConf -> user.id
            }
    }
}
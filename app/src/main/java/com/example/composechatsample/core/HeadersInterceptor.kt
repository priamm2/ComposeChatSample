package com.example.composechatsample.core

import okhttp3.Interceptor
import okhttp3.Response

internal class HeadersInterceptor(private val isAnonymous: () -> Boolean) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authType = if (isAnonymous()) "anonymous" else "jwt"
        val request = chain.request()
            .newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("stream-auth-type", authType)
            .addHeader("X-Stream-Client", ChatClient.buildSdkTrackingHeaders())
            .addHeader("Cache-Control", "no-cache")
            .build()
        return chain.proceed(request)
    }
}
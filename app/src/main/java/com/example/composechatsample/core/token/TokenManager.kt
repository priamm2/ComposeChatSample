package com.example.composechatsample.core.token

internal interface TokenManager {
    fun ensureTokenLoaded()
    fun loadSync(): String
    fun expireToken()
    fun hasTokenProvider(): Boolean
    fun setTokenProvider(provider: CacheableTokenProvider)
    fun getToken(): String
    fun hasToken(): Boolean
}
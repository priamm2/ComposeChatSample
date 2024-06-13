package com.example.composechatsample.core.token

internal class CacheableTokenProvider(private val tokenProvider: TokenProvider) : TokenProvider {
    private var cachedToken = ""
    override fun loadToken(): String = tokenProvider.loadToken().also { cachedToken = it }
    fun getCachedToken(): String = cachedToken
}
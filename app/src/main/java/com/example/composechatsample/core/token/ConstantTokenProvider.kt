package com.example.composechatsample.core.token

internal class ConstantTokenProvider(private val token: String) : TokenProvider {
    override fun loadToken(): String = token
}
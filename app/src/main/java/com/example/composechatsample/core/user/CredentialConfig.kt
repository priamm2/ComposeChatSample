package com.example.composechatsample.core.user

public class CredentialConfig(
    public val userId: String,
    public val userToken: String,
    public val userName: String,
    public val isAnonymous: Boolean,
) {
    internal fun isValid(): Boolean = userId.isNotEmpty() && userToken.isNotEmpty() && userName.isNotEmpty()
}
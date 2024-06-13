package com.example.composechatsample.core.user

public interface UserCredentialStorage {
    public fun put(credentialConfig: CredentialConfig)

    public fun get(): CredentialConfig?
    public fun clear()
}
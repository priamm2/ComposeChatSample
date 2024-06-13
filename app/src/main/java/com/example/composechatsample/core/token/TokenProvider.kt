package com.example.composechatsample.core.token

import androidx.annotation.WorkerThread

public interface TokenProvider {

    @WorkerThread
    public fun loadToken(): String
}
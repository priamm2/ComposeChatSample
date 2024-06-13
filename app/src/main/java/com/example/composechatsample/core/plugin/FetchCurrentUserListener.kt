package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.User

public interface FetchCurrentUserListener {

    public suspend fun onFetchCurrentUserResult(result: Result<User>)
}
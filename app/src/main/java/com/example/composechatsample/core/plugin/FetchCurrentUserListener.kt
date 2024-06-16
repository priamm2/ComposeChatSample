package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.User
import com.example.composechatsample.core.Result

public interface FetchCurrentUserListener {

    public suspend fun onFetchCurrentUserResult(result: Result<User>)
}
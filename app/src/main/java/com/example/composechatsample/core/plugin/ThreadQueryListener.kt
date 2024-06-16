package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.Message

import com.example.composechatsample.core.Result

public interface ThreadQueryListener {

    public suspend fun onGetRepliesPrecondition(parentId: String): Result<Unit> = Result.Success(Unit)

    public suspend fun onGetRepliesRequest(parentId: String, limit: Int)

    public suspend fun onGetRepliesResult(result: Result<List<Message>>, parentId: String, limit: Int)

    public suspend fun onGetRepliesMoreRequest(
        parentId: String,
        firstId: String,
        limit: Int,
    )

    public suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        parentId: String,
        firstId: String,
        limit: Int,
    )

    public suspend fun onGetNewerRepliesRequest(
        parentId: String,
        limit: Int,
        lastId: String?,
    )

    public suspend fun onGetNewerRepliesResult(
        result: Result<List<Message>>,
        parentId: String,
        limit: Int,
        lastId: String?,
    )
}
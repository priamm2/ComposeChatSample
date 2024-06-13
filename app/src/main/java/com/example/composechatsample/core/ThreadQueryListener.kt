package com.example.composechatsample.core

import com.example.composechatsample.core.models.Message

public interface ThreadQueryListener {

    public suspend fun onGetRepliesPrecondition(parentId: String): Result<Unit> = Result.Success(Unit)

    /** Runs side effect before the request is launched. */
    public suspend fun onGetRepliesRequest(parentId: String, limit: Int)

    /** Runs this function on the result of the [ChatClient.getReplies] request. */
    public suspend fun onGetRepliesResult(result: Result<List<Message>>, parentId: String, limit: Int)

    /** Runs side effect before the request is launched. */
    public suspend fun onGetRepliesMoreRequest(
        parentId: String,
        firstId: String,
        limit: Int,
    )

    /** Runs this function on the result of the [ChatClient.getRepliesMore] request. */
    public suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        parentId: String,
        firstId: String,
        limit: Int,
    )

    /** Runs side effect before the request is launched. */
    public suspend fun onGetNewerRepliesRequest(
        parentId: String,
        limit: Int,
        lastId: String?,
    )

    /** Runs this function on the result of the [ChatClient.getNewerReplies] request. */
    public suspend fun onGetNewerRepliesResult(
        result: Result<List<Message>>,
        parentId: String,
        limit: Int,
        lastId: String?,
    )
}
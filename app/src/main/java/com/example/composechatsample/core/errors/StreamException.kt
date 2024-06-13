package com.example.composechatsample.core.errors

public sealed class StreamException : Exception {
    protected constructor() : super()
    protected constructor(message: String?) : super(message)
    protected constructor(message: String?, cause: Throwable?) : super(message, cause)
    protected constructor(cause: Throwable?) : super(cause)
}
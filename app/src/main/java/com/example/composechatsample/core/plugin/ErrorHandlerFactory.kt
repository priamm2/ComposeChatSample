package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.errors.ErrorHandler

public interface ErrorHandlerFactory {

    public fun create(): ErrorHandler
}
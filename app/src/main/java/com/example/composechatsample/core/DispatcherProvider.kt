package com.example.composechatsample.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher

public object DispatcherProvider {

    public var Main: CoroutineDispatcher = Dispatchers.Main
        internal set

    public val Immediate: CoroutineDispatcher
        get() {
            val mainDispatcher = Main

            return if (mainDispatcher is MainCoroutineDispatcher) {
                mainDispatcher.immediate
            } else {
                mainDispatcher
            }
        }

    public var IO: CoroutineDispatcher = Dispatchers.IO
        internal set

    public fun set(mainDispatcher: CoroutineDispatcher, ioDispatcher: CoroutineDispatcher) {
        Main = mainDispatcher
        IO = ioDispatcher
    }

    public fun reset() {
        Main = Dispatchers.Main
        IO = Dispatchers.IO
    }
}
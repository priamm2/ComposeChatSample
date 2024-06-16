package com.example.composechatsample.core

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal class ThreadLocalDelegate<T : Any>(
    private val value: () -> T,
) : ReadOnlyProperty<Any?, T> {

    private val threadLocal = object : ThreadLocal<T>() {
        override fun initialValue(): T? = value()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = threadLocal.get()!!
}
package com.example.composechatsample.common

public fun interface QueryFilter<T> {

    public fun filter(items: List<T>, query: String): List<T>
}
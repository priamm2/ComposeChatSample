package com.example.composechatsample

data class KeyValuePair<out K, out V>(
    val key: K,
    val value: V,
) {
    override fun toString(): String = "($key, $value)"
}
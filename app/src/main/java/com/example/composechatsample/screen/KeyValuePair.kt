package com.example.composechatsample.screen

public data class KeyValuePair<out K, out V>(
    public val key: K,
    public val value: V,
) {
    public override fun toString(): String = "($key, $value)"
}
package com.example.composechatsample.core.extensions

private val snakeRegex = "_[a-zA-Z]".toRegex()
private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

internal fun String.snakeToLowerCamelCase(): String {
    return snakeRegex.replace(this) { matchResult ->
        matchResult.value.replace("_", "").uppercase()
    }
}

internal fun String.lowerCamelCaseToGetter(): String = "get${this[0].uppercase()}${this.substring(1)}"

internal fun String.camelCaseToSnakeCase(): String {
    return camelRegex.replace(this) { "_${it.value}" }.lowercase()
}

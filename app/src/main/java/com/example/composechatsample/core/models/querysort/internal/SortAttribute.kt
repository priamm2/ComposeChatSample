package com.example.composechatsample.core.models.querysort.internal

import kotlin.reflect.KProperty1

sealed class SortAttribute<T> {
    abstract val name: String

    data class FieldSortAttribute<T>(val field: KProperty1<T, Comparable<*>?>, override val name: String) :
        SortAttribute<T>()
    data class FieldNameSortAttribute<T>(override val name: String) : SortAttribute<T>()
}

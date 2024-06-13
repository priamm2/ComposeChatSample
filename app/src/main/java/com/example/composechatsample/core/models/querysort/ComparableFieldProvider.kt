package com.example.composechatsample.core.models.querysort

interface ComparableFieldProvider {
    fun getComparableField(fieldName: String): Comparable<*>?
}

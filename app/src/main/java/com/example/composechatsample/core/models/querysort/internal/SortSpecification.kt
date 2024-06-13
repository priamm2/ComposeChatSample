package com.example.composechatsample.core.models.querysort.internal

import com.example.composechatsample.core.models.querysort.SortDirection

data class SortSpecification<T>(
    val sortAttribute: SortAttribute<T>,
    val sortDirection: SortDirection,
)

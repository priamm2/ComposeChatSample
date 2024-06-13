package com.example.composechatsample.screen

import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.querysort.QuerySorter

public data class QueryConfig<T : Any>(
    val filters: FilterObject,
    val querySort: QuerySorter<T>,
)
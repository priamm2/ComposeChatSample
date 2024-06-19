package com.example.composechatsample.common

public class Combine(
    private val transformers: List<QueryFormatter>,
) : QueryFormatter {

    public constructor(vararg transformers: QueryFormatter) : this(transformers.toList())

    override fun format(query: String): String {
        return transformers.fold(query) { acc, transformer -> transformer.format(acc) }
    }
}
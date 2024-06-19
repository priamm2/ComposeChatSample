package com.example.composechatsample.common

public class Transliterate(
    private val transliterator: StreamTransliterator = DefaultStreamTransliterator(),
) : QueryFormatter {
    override fun format(query: String): String {
        if (query.isEmpty()) return query
        return transliterator.transliterate(query)
    }
}
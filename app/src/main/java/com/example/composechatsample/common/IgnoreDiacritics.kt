package com.example.composechatsample.common

import java.text.Normalizer

internal class IgnoreDiacritics : QueryFormatter {

    private val diacriticsRegex = "\\p{InCombiningDiacriticalMarks}+".toRegex()
    override fun format(query: String): String {
        return query.removeDiacritics()
    }

    private fun String.removeDiacritics(): String {
        if (this.isEmpty()) return this
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        return normalized.replace(diacriticsRegex, "")
    }
}
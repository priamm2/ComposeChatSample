package com.example.composechatsample.core.models.querysort

enum class SortDirection(val value: Int) {

    DESC(-1),
    ASC(1),
    ;

    companion object {
        fun fromNumber(value: Int): SortDirection =
            when (value) {
                1 -> ASC
                -1 -> DESC
                else -> throw IllegalArgumentException("Unsupported sort direction")
            }
    }
}

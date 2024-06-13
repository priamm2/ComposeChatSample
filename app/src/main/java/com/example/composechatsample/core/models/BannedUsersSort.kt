package com.example.composechatsample.core.models

import androidx.compose.runtime.Immutable
import com.example.composechatsample.core.models.querysort.ComparableFieldProvider
import java.util.Date

@Immutable
public data class BannedUsersSort(val createdAt: Date) : ComparableFieldProvider {

    override fun getComparableField(fieldName: String): Comparable<*>? {
        return when (fieldName) {
            "created_at", "createdAt" -> createdAt
            else -> null
        }
    }
}

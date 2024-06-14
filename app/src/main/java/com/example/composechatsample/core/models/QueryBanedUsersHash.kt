package com.example.composechatsample.core.models

import com.example.composechatsample.core.models.querysort.QuerySorter
import java.util.Date

internal data class QueryBanedUsersHash(
    val filter: FilterObject,
    val sort: QuerySorter<BannedUsersSort>,
    val offset: Int?,
    val limit: Int?,
    val createdAtAfter: Date?,
    val createdAtAfterOrEqual: Date?,
    val createdAtBefore: Date?,
    val createdAtBeforeOrEqual: Date?,
)
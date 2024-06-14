package com.example.composechatsample.core.models.mapper

import com.example.composechatsample.core.models.AndFilterObject
import com.example.composechatsample.core.models.AutocompleteFilterObject
import com.example.composechatsample.core.models.ContainsFilterObject
import com.example.composechatsample.core.models.DistinctFilterObject
import com.example.composechatsample.core.models.EqualsFilterObject
import com.example.composechatsample.core.models.ExistsFilterObject
import com.example.composechatsample.core.models.FilterObject
import com.example.composechatsample.core.models.GreaterThanFilterObject
import com.example.composechatsample.core.models.GreaterThanOrEqualsFilterObject
import com.example.composechatsample.core.models.InFilterObject
import com.example.composechatsample.core.models.LessThanFilterObject
import com.example.composechatsample.core.models.LessThanOrEqualsFilterObject
import com.example.composechatsample.core.models.NeutralFilterObject
import com.example.composechatsample.core.models.NorFilterObject
import com.example.composechatsample.core.models.NotEqualsFilterObject
import com.example.composechatsample.core.models.NotExistsFilterObject
import com.example.composechatsample.core.models.NotInFilterObject
import com.example.composechatsample.core.models.OrFilterObject

internal fun FilterObject.toMap(): Map<String, Any> = when (this) {
    is AndFilterObject -> mapOf(KEY_AND to this.filterObjects.map(FilterObject::toMap))
    is OrFilterObject -> mapOf(KEY_OR to this.filterObjects.map(FilterObject::toMap))
    is NorFilterObject -> mapOf(KEY_NOR to this.filterObjects.map(FilterObject::toMap))
    is ExistsFilterObject -> mapOf(this.fieldName to mapOf(KEY_EXIST to true))
    is NotExistsFilterObject -> mapOf(this.fieldName to mapOf(KEY_EXIST to false))
    is EqualsFilterObject -> mapOf(this.fieldName to this.value)
    is NotEqualsFilterObject -> mapOf(this.fieldName to mapOf(KEY_NOT_EQUALS to this.value))
    is ContainsFilterObject -> mapOf(this.fieldName to mapOf(KEY_CONTAINS to this.value))
    is GreaterThanFilterObject -> mapOf(this.fieldName to mapOf(KEY_GREATER_THAN to this.value))
    is GreaterThanOrEqualsFilterObject -> mapOf(this.fieldName to mapOf(KEY_GREATER_THAN_OR_EQUALS to this.value))
    is LessThanFilterObject -> mapOf(this.fieldName to mapOf(KEY_LESS_THAN to this.value))
    is LessThanOrEqualsFilterObject -> mapOf(this.fieldName to mapOf(KEY_LESS_THAN_OR_EQUALS to this.value))
    is InFilterObject -> mapOf(this.fieldName to mapOf(KEY_IN to this.values))
    is NotInFilterObject -> mapOf(this.fieldName to mapOf(KEY_NOT_IN to this.values))
    is AutocompleteFilterObject -> mapOf(this.fieldName to mapOf(KEY_AUTOCOMPLETE to this.value))
    is DistinctFilterObject -> mapOf(KEY_DISTINCT to true, KEY_MEMBERS to this.memberIds)
    is NeutralFilterObject -> emptyMap<String, String>()
}

private const val KEY_EXIST: String = "\$exists"
private const val KEY_CONTAINS: String = "\$contains"
private const val KEY_AND: String = "\$and"
private const val KEY_OR: String = "\$or"
private const val KEY_NOR: String = "\$nor"
private const val KEY_NOT_EQUALS: String = "\$ne"
private const val KEY_GREATER_THAN: String = "\$gt"
private const val KEY_GREATER_THAN_OR_EQUALS: String = "\$gte"
private const val KEY_LESS_THAN: String = "\$lt"
private const val KEY_LESS_THAN_OR_EQUALS: String = "\$lte"
private const val KEY_IN: String = "\$in"
private const val KEY_NOT_IN: String = "\$nin"
private const val KEY_AUTOCOMPLETE: String = "\$autocomplete"
private const val KEY_DISTINCT: String = "distinct"
private const val KEY_MEMBERS: String = "members"
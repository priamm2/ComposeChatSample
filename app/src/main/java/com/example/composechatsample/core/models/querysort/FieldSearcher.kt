package com.example.composechatsample.core.models.querysort

import com.example.composechatsample.core.extensions.lowerCamelCaseToGetter
import com.example.composechatsample.core.extensions.snakeToLowerCamelCase
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class FieldSearcher {

    internal fun <T : Any> findComparableMemberProperty(
        fieldName: String,
        kClass: KClass<T>,
    ): KProperty1<T, Comparable<*>?>? {
        val members = kClass.members.filterIsInstance<KProperty1<T, Comparable<*>?>>()
        val camelCaseName = fieldName.snakeToLowerCamelCase()
        val getField = camelCaseName.lowerCamelCaseToGetter()

        return members.firstOrNull { property ->
            property.name == camelCaseName || property.name == getField
        }
    }

    internal fun findComparable(
        any: Any,
        fieldName: String,
    ): Comparable<Any>? {
        val members = any::class.members
        val camelCaseName = fieldName.snakeToLowerCamelCase()
        val getField = camelCaseName.lowerCamelCaseToGetter()

        return members.firstOrNull { property ->
            property.name == camelCaseName || property.name == getField
        }?.call(any) as? Comparable<Any>
    }
}

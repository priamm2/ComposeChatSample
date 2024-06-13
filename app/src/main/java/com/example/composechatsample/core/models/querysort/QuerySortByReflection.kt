package com.example.composechatsample.core.models.querysort

import com.example.composechatsample.core.extensions.camelCaseToSnakeCase
import com.example.composechatsample.core.models.CustomObject
import com.example.composechatsample.core.models.querysort.BaseQuerySort
import com.example.composechatsample.core.models.querysort.FieldSearcher
import com.example.composechatsample.core.models.querysort.internal.SortAttribute
import com.example.composechatsample.core.models.querysort.internal.SortSpecification
import com.example.composechatsample.core.models.querysort.internal.compare

import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import com.example.composechatsample.log.taggedLogger


@Suppress("TooManyFunctions")
public open class QuerySortByReflection<T : Any> : BaseQuerySort<T>() {
    private val logger by taggedLogger("QuerySort")

    private val fieldSearcher: FieldSearcher = FieldSearcher()

    override fun comparatorFromFieldSort(
        firstSort: SortAttribute.FieldSortAttribute<T>,
        sortDirection: SortDirection,
    ): Comparator<T> =
        firstSort.field.comparator(sortDirection)

    override fun comparatorFromNameAttribute(
        name: SortAttribute.FieldNameSortAttribute<T>,
        sortDirection: SortDirection,
    ): Comparator<T> =
        name.name.comparator(sortDirection)

    @Suppress("UNCHECKED_CAST")
    private fun KProperty1<T, Comparable<*>?>.comparator(sortDirection: SortDirection): Comparator<T> =
        this.let { compareProperty ->
            Comparator { c0, c1 ->
                compare(
                    (compareProperty.getter.call(c0) as? Comparable<Any>),
                    (compareProperty.getter.call(c1) as? Comparable<Any>),
                    sortDirection,
                )
            }
        }

    @Suppress("UNCHECKED_CAST")
    private fun String.comparator(sortDirection: SortDirection): Comparator<T> =
        Comparator { o1, o2 ->
            compare(
                comparableFromExtraData(o1, this) ?: fieldSearcher.findComparable(o1, this),
                comparableFromExtraData(o2, this) ?: fieldSearcher.findComparable(o2, this),
                sortDirection,
            )
        }

    private fun comparableFromExtraData(any: Any, field: String): Comparable<Any>? {
        return (any as? CustomObject)?.extraData?.get(field) as? Comparable<Any>
    }

    internal open fun add(sortSpecification: SortSpecification<T>): QuerySortByReflection<T> {
        sortSpecifications = sortSpecifications + sortSpecification
        return this
    }

    public open fun asc(field: KProperty1<T, Comparable<*>?>): QuerySortByReflection<T> {
        return add(
            SortSpecification(
                SortAttribute.FieldSortAttribute(field, field.name.camelCaseToSnakeCase()),
                SortDirection.ASC,
            ),
        )
    }

    public open fun desc(field: KProperty1<T, Comparable<*>?>): QuerySortByReflection<T> {
        return add(
            SortSpecification(
                SortAttribute.FieldSortAttribute(
                    field,
                    field.name.camelCaseToSnakeCase(),
                ),
                SortDirection.DESC,
            ),
        )
    }

    public open fun asc(fieldName: String, javaClass: Class<T>): QuerySortByReflection<T> {
        return add(SortSpecification(getSortFeature(fieldName, javaClass), SortDirection.ASC))
    }

    public open fun desc(fieldName: String, javaClass: Class<T>): QuerySortByReflection<T> {
        return add(SortSpecification(getSortFeature(fieldName, javaClass), SortDirection.DESC))
    }

    public open fun asc(fieldName: String): QuerySortByReflection<T> {
        return add(
            SortSpecification(
                SortAttribute.FieldNameSortAttribute(fieldName),
                SortDirection.ASC,
            ),
        )
    }

    public open fun desc(fieldName: String): QuerySortByReflection<T> {
        return add(
            SortSpecification(
                SortAttribute.FieldNameSortAttribute(fieldName),
                SortDirection.DESC,
            ),
        )
    }

    public open fun asc(fieldName: String, kClass: KClass<T>): QuerySortByReflection<T> {
        return add(SortSpecification(getSortFeature(fieldName, kClass), SortDirection.ASC))
    }

    public open fun desc(fieldName: String, kClass: KClass<T>): QuerySortByReflection<T> {
        return add(SortSpecification(getSortFeature(fieldName, kClass), SortDirection.DESC))
    }

    internal fun toList(): List<Pair<String, SortDirection>> =
        sortSpecifications.map { it.sortAttribute.name to it.sortDirection }

    private fun getSortFeature(fieldName: String, javaClass: Class<T>): SortAttribute<T> {
        @Suppress("UNCHECKED_CAST")
        val kClass = Reflection.createKotlinClass(javaClass) as KClass<T>
        return getSortFeature(fieldName, kClass)
    }

    private fun getSortFeature(fieldName: String, kClass: KClass<T>): SortAttribute<T> {
        return fieldSearcher.findComparableMemberProperty(fieldName, kClass)
            ?.let { SortAttribute.FieldSortAttribute(it, fieldName) }
            .also { fieldSortAttribute ->
                logger.d { "[getSortFeature] A field to sort was found. Using field: $fieldSortAttribute" }
            }
            ?: SortAttribute.FieldNameSortAttribute(fieldName)
    }

    public companion object {
        public inline fun <reified T : Any> QuerySortByReflection<T>.ascByName(
            fieldName: String,
        ): QuerySortByReflection<T> = asc(fieldName, T::class)

        public inline fun <reified T : Any> QuerySortByReflection<T>.descByName(
            fieldName: String,
        ): QuerySortByReflection<T> = desc(fieldName, T::class)

        public inline fun <reified T : Any> asc(fieldName: String): QuerySortByReflection<T> =
            QuerySortByReflection<T>().ascByName(fieldName)

        public inline fun <reified R : Any> desc(fieldName: String): QuerySortByReflection<R> =
            QuerySortByReflection<R>().descByName(fieldName)

        public fun <T : Any> asc(field: KProperty1<T, Comparable<*>?>): QuerySortByReflection<T> =
            QuerySortByReflection<T>().asc(field)

        public fun <T : Any> desc(field: KProperty1<T, Comparable<*>?>): QuerySortByReflection<T> =
            QuerySortByReflection<T>().desc(field)
    }
}

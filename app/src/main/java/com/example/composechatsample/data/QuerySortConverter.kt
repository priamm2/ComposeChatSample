package com.example.composechatsample.data

import androidx.room.TypeConverter
import com.example.composechatsample.core.models.Channel
import com.example.composechatsample.core.models.querysort.QuerySortByField
import com.example.composechatsample.core.models.querysort.QuerySorter
import com.example.composechatsample.core.models.querysort.SortDirection
import com.squareup.moshi.adapter

internal class QuerySortConverter {

    @OptIn(ExperimentalStdlibApi::class)
    private val adapter = moshi.adapter<List<Map<String, Any>>>()

    @TypeConverter
    fun stringToObject(data: String?): QuerySorter<Channel> {
        if (data.isNullOrEmpty()) {
            return QuerySortByField()
        }
        val listOfSortSpec = adapter.fromJson(data)
        return listOfSortSpec?.let(::parseQuerySort) ?: QuerySortByField()
    }

    private fun parseQuerySort(listOfSortSpec: List<Map<String, Any>>): QuerySorter<Channel> {
        return listOfSortSpec.fold(QuerySortByField()) { sort, sortSpecMap ->
            val fieldName = sortSpecMap[QuerySorter.KEY_FIELD_NAME] as? String
                ?: error("Cannot parse sortSpec to query sort\n$sortSpecMap")
            val direction = (sortSpecMap[QuerySorter.KEY_DIRECTION] as? Number)?.toInt()
                ?: error("Cannot parse sortSpec to query sort\n$sortSpecMap")
            when (direction) {
                SortDirection.ASC.value -> sort.asc(fieldName)
                SortDirection.DESC.value -> sort.desc(fieldName)
                else -> error("Cannot parse sortSpec to query sort\n$sortSpecMap")
            }
        }
    }

    /**
     * @return Nullable [String] to let KSP know this function cannot be used for "Nullable to NonNull" converting.
     *
     * An example of the incorrect behaviour:
     *     val stringifiedObject: String? = anotherConverter.objectToString(...)
     *     val stringList: List<String>? = QuerySortConverter.stringToObject(stringifiedObject)
     *     val nonNullStringifiedObject: String = QuerySortConverter.objectToString(stringifiedObject)
     *     // ... binding nonNullStringifiedObject to table's column
     *
     * The current behavior:
     *     val stringifiedObject: String? = anotherConverter.objectToString(...)
     *     // ... binding stringifiedObject to table's column
     */
    @TypeConverter
    fun objectToString(querySort: QuerySorter<Channel>): String? {
        return adapter.toJson(querySort.toDto())
    }
}
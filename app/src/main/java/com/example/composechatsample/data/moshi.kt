package com.example.composechatsample.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.MultiMapJsonAdapter
import com.squareup.moshi.addAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@OptIn(ExperimentalStdlibApi::class)
val moshi = Moshi.Builder()
    .addAdapter(DateAdapter())
    .add(KotlinJsonAdapterFactory())
    .add(MultiMapJsonAdapter.FACTORY)
    .build()
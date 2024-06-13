package com.example.composechatsample.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.addAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@OptIn(ExperimentalStdlibApi::class)
internal val moshi: Moshi = Moshi.Builder()
    .addAdapter(DateAdapter())
    .add(KotlinJsonAdapterFactory())
    .add(MultiMapJsonAdapter.FACTORY)
    .build()
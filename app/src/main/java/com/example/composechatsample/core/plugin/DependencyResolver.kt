package com.example.composechatsample.core.plugin

import kotlin.reflect.KClass

public interface DependencyResolver {

    public fun <T : Any> resolveDependency(klass: KClass<T>): T?
}
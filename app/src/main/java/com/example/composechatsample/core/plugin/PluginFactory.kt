package com.example.composechatsample.core.plugin

import com.example.composechatsample.core.models.User

public interface PluginFactory : DependencyResolver {
    public fun get(user: User): Plugin
}
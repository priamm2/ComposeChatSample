package com.example.composechatsample.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

internal interface ClientScope : CoroutineScope

internal fun ClientScope(): ClientScope = ClientScopeImpl()

private class ClientScopeImpl :
    ClientScope,
    CoroutineScope by CoroutineScope(
        SupervisorJob() + DispatcherProvider.IO + SharedCalls(),
    )
package com.example.composechatsample.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

public class FiniteStateMachine<S : Any, E : Any>(
    initialState: S,
    private val stateFunctions: Map<KClass<out S>, Map<KClass<out E>, StateFunction<S, E>>>,
    private val defaultEventHandler: (S, E) -> S,
) {
    private val mutex = Mutex()
    private val _state: MutableStateFlow<S> = MutableStateFlow(initialState)

    public val stateFlow: StateFlow<S> = _state

    public val state: S
        get() = _state.value

    public suspend fun sendEvent(event: E) {
        mutex.withLock {
            val currentState = _state.value
            val handler = stateFunctions[currentState::class]?.get(event::class) ?: defaultEventHandler
            _state.value = handler(currentState, event)
        }
    }

    public fun stay(): S = state

    public companion object {
        public operator fun <S : Any, E : Any> invoke(builder: FSMBuilder<S, E>.() -> Unit): FiniteStateMachine<S, E> {
            return FSMBuilder<S, E>().apply(builder).build()
        }
    }
}
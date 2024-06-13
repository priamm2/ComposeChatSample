package com.example.composechatsample.core

import com.example.composechatsample.core.events.ChatEvent
import com.example.composechatsample.core.events.ConnectedEvent
import com.example.composechatsample.core.events.ConnectingEvent
import com.example.composechatsample.core.events.DisconnectedEvent
import com.example.composechatsample.core.events.ErrorEvent
import com.example.composechatsample.core.models.ConnectionData
import com.example.composechatsample.core.models.EventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date

internal class ChatEventsObservable(
    private val waitConnection: FlowCollector<Result<ConnectionData>>,
    private val scope: CoroutineScope,
    private val chatSocket: ChatSocket,
) {

    private val mutex = Mutex()

    private val subscriptions = mutableSetOf<EventSubscription>()
    private val eventsMapper = EventsMapper(this)

    private fun onNext(event: ChatEvent) {
        notifySubscriptions(event)
        emitConnectionEvents(event)
    }

    private fun emitConnectionEvents(event: ChatEvent) {
        scope.launch {
            when (event) {
                is ConnectedEvent -> {
                    waitConnection.emit(Result.Success(ConnectionData(event.me, event.connectionId)))
                }
                is ErrorEvent -> {
                    waitConnection.emit(Result.Failure(event.error))
                }
                else -> Unit // Ignore other events
            }
        }
    }

    fun subscribe(
        filter: (ChatEvent) -> Boolean = { true },
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return addSubscription(SubscriptionImpl(filter, listener))
    }

    fun subscribeSuspend(
        filter: (ChatEvent) -> Boolean = { true },
        listener: ChatEventSuspendListener<ChatEvent>,
    ): Disposable {
        return addSubscription(SuspendSubscription(scope, filter, listener))
    }

    fun subscribeSingle(
        filter: (ChatEvent) -> Boolean = { true },
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return addSubscription(
            SubscriptionImpl(filter, listener).apply {
                afterEventDelivered = this::dispose
            },
        )
    }

    private fun notifySubscriptions(event: ChatEvent) {
        scope.launch {
            mutex.withLock {
                val iterator = subscriptions.iterator()
                while (iterator.hasNext()) {
                    val subscription = iterator.next()
                    if (subscription.isDisposed) {
                        iterator.remove()
                    } else {
                        subscription.onNext(event)
                    }
                }
                if (subscriptions.isEmpty()) {
                    chatSocket.removeListener(eventsMapper)
                }
            }
        }
    }

    private fun addSubscription(subscription: EventSubscription): Disposable {
        scope.launch {
            mutex.withLock {
                if (subscriptions.isEmpty()) {
                    chatSocket.addListener(eventsMapper)
                }
                subscriptions.add(subscription)
            }
        }
        return subscription
    }

    internal fun interface ChatEventSuspendListener<EventT : ChatEvent> {
        suspend fun onEvent(event: EventT)
    }

    private class EventsMapper(private val observable: ChatEventsObservable) : SocketListener() {

        override val deliverOnMainThread: Boolean
            get() = false

        override fun onConnecting() {
            observable.onNext(ConnectingEvent(EventType.CONNECTION_CONNECTING, Date(), null))
        }

        override fun onConnected(event: ConnectedEvent) {
            observable.onNext(event)
        }

        override fun onDisconnected(cause: DisconnectCause) {
            observable.onNext(
                DisconnectedEvent(
                    EventType.CONNECTION_DISCONNECTED,
                    createdAt = Date(),
                    disconnectCause = cause,
                    rawCreatedAt = null,
                ),
            )
        }

        override fun onEvent(event: ChatEvent) {
            observable.onNext(event)
        }

        override fun onError(error: Error) {
            observable.onNext(
                ErrorEvent(
                    EventType.CONNECTION_ERROR,
                    createdAt = Date(),
                    error = error,
                    rawCreatedAt = null,
                ),
            )
        }
    }
}
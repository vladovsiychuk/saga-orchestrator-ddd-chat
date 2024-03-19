package com.rest_service.commons

import com.rest_service.commons.enums.EventType
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import reactor.core.publisher.Mono

abstract class AbstractEventHandler(private val applicationEventPublisher: ApplicationEventPublisher<DomainEvent>) {
    protected abstract fun shouldHandle(eventType: EventType): Boolean
    protected abstract fun rebuildState(event: DomainEvent): Mono<State>
    protected abstract fun saveEvent(newEvent: DomainEvent): Mono<Boolean>
    protected abstract fun handleError(event: DomainEvent, error: Throwable): Mono<Void>

    @EventListener
    @Async
    open fun messageActionListener(event: DomainEvent) {
        if (shouldHandle(event.type))
            handleSagaEvent(event)
    }

    private fun handleSagaEvent(event: DomainEvent) {
        rebuildState(event)
            .flatMap { state -> state.apply(event).thenReturn(state) }
            .flatMap { state -> saveEvent(event).thenReturn(state) }
            .flatMap { it.createNextEvent() }
            .doOnNext { nextEvent -> applicationEventPublisher.publishEventAsync(nextEvent) }
            .then()
            .onErrorResume { handleError(event, it) }
            .subscribe()
    }
}

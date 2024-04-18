package com.rest_service.commons

import io.micronaut.context.event.ApplicationEventPublisher
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

abstract class AbstractEventHandler(private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>) {
    protected abstract fun rebuildDomainFromEvent(event: DomainEvent): Mono<Domain>
    protected abstract fun mapDomainEvent(event: SagaEvent): DomainEvent
    protected abstract fun saveEvent(event: DomainEvent): Mono<DomainEvent>
    protected abstract fun handleError(event: SagaEvent, error: Throwable): Mono<Void>

    fun handleEvent(sagaEvent: SagaEvent) {
        mapDomainEvent(sagaEvent).toMono()
            .flatMap { domainEvent -> saveEvent(domainEvent) }
            .flatMap { savedEvent -> rebuildDomainFromEvent(savedEvent) }
            .flatMap { domain -> domain.createResponseSagaEvent(sagaEvent) }
            .doOnNext { responseEvent -> applicationEventPublisher.publishEventAsync(responseEvent) }
            .then()
            .onErrorResume { handleError(sagaEvent, it) }
            .subscribe()
    }
}

package com.rest_service.commons

import io.micronaut.context.event.ApplicationEventPublisher
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

abstract class AbstractEventHandler(private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>) {
    protected abstract fun rebuildDomain(event: SagaEvent): Mono<Domain>
    protected abstract fun mapDomainEvent(event: SagaEvent): DomainEvent
    protected abstract fun saveEvent(event: DomainEvent): Mono<Boolean>
    protected abstract fun handleError(event: SagaEvent, error: Throwable): Mono<Void>

    fun handleEvent(sagaEvent: SagaEvent) {
        rebuildDomain(sagaEvent).zipWith(mapDomainEvent(sagaEvent).toMono())
        { domain, domainEvent ->
            domain.apply(domainEvent).toMono()
                .flatMap { saveEvent(it) }
                .flatMap { domain.createResponseSagaEvent() }
                .doOnNext { responseEvent -> applicationEventPublisher.publishEventAsync(responseEvent) }
        }
            .flatMap { it }
            .then()
            .onErrorResume { handleError(sagaEvent, it) }
            .subscribe()
    }
}

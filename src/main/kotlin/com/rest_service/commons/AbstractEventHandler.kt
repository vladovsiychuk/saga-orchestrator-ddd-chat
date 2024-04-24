package com.rest_service.commons

import io.micronaut.context.event.ApplicationEventPublisher
import reactor.core.publisher.Mono

abstract class AbstractEventHandler(private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>) {
    protected abstract fun rebuildDomainFromEvent(domainEvent: DomainEvent): Mono<Domain>
    protected abstract fun mapDomainEvent(): Mono<DomainEvent>
    protected abstract fun saveEvent(domainEvent: DomainEvent): Mono<DomainEvent>
    protected abstract fun handleError(error: Throwable): Mono<Void>
    protected abstract fun createResponseSagaEvent(domain: Domain): Mono<SagaEvent>

    fun handleEvent() {
        mapDomainEvent()
            .flatMap { domainEvent -> saveEvent(domainEvent) }
            .flatMap { savedEvent -> rebuildDomainFromEvent(savedEvent) }
            .flatMap { domain -> createResponseSagaEvent(domain) }
            .doOnNext { responseEvent -> applicationEventPublisher.publishEventAsync(responseEvent) }
            .then()
            .onErrorResume { handleError(it) }
            .subscribe()
    }
}

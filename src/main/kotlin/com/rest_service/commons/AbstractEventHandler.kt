package com.rest_service.commons

import io.micronaut.context.event.ApplicationEventPublisher
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

abstract class AbstractEventHandler(private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>) {
    protected abstract fun rebuildDomain(event: SagaEvent): Mono<Domain>
    protected abstract fun mapDomainEvent(event: SagaEvent): DomainEvent
    protected abstract fun saveEvent(event: DomainEvent): Mono<Boolean>
    protected abstract fun handleError(event: SagaEvent, error: Throwable): Mono<Void>
    protected abstract fun checkOperationFailed(operationId: UUID): Mono<Boolean>

    fun handleEvent(sagaEvent: SagaEvent) {
        checkOperationFailed(sagaEvent.operationId)
            .then(mapDomainEvent(sagaEvent).toMono())
            .flatMap { domainEvent -> saveEvent(domainEvent) }
            .flatMap { rebuildDomain(sagaEvent) }
            .flatMap { domain -> domain.createResponseSagaEvent() }
            .doOnNext { responseEvent -> applicationEventPublisher.publishEventAsync(responseEvent) }
            .then()
            .onErrorResume { handleError(sagaEvent, it) }
            .subscribe()
    }
}

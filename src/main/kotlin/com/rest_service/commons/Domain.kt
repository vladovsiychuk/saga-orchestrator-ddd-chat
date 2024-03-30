package com.rest_service.commons

import reactor.core.publisher.Mono

interface Domain {
    fun apply(event: DomainEvent): DomainEvent
    fun createResponseSagaEvent(): Mono<SagaEvent>
}

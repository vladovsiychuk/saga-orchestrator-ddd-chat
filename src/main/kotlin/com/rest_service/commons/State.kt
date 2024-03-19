package com.rest_service.commons

import com.rest_service.saga_orchestrator.infrastructure.SagaEvent
import reactor.core.publisher.Mono

interface State {
    fun apply(event: SagaEvent): Mono<Boolean>
    fun createNextEvent(): Mono<DomainEvent>
}
